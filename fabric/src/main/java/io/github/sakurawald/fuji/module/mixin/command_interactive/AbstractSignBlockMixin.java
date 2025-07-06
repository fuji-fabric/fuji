package io.github.sakurawald.fuji.module.mixin.command_interactive;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.module.initializer.command_interactive.CommandInteractiveInitializer;
import net.minecraft.block.AbstractSignBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.SignText;
import net.minecraft.entity.player.PlayerEntity;
#if MC_VER <= MC_1_20_4
import net.minecraft.util.Hand;
#elif MC_VER > MC_1_20_4
#endif

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Mixin(AbstractSignBlock.class)
public class AbstractSignBlockMixin {

    @Unique
    private static final String COMMAND_STRING_SPLIT_CHARACTER = "/";

    #if MC_VER <= MC_1_20_4
             @Inject(method = "onUse", at = @At("HEAD"), cancellable = true)
             private void listenSignBlockUse(BlockState blockState, World world, BlockPos blockPos, PlayerEntity player, Hand hand, BlockHitResult blockHitResult, CallbackInfoReturnable<ActionResult> cir)
             #elif MC_VER > MC_1_20_4
    @Inject(method = "onUse", at = @At("HEAD"), cancellable = true)
    private void listenSignBlockUse(BlockState blockState, @NotNull World world, BlockPos blockPos, @NotNull PlayerEntity player, BlockHitResult blockHitResult, @NotNull CallbackInfoReturnable<ActionResult> cir)
    #endif
    {
        /* Check if the player can use interactive command now. */
        if (canUseInteractiveCommand(player)) return;

        /* Check if the player is server side. */
        if (!PlayerHelper.isServerPlayer(player)) return;

        /* Extract the sign lines from the sign block. */
        BlockEntity interactingBlockEntity = world.getBlockEntity(blockPos);
        if (interactingBlockEntity instanceof SignBlockEntity signBlockEntity) {
            SignText facingSignText = signBlockEntity.getText(signBlockEntity.isPlayerFacingFront(player));
            String facingSignLines = extractSignLines(facingSignText);
            if (facingSignLines.contains(COMMAND_STRING_SPLIT_CHARACTER)) {
                /* Consume this interaction. */
                cir.setReturnValue(ActionResult.CONSUME);

                /* Send command execution packets. */
                List<String> commands = splitCommands(facingSignLines)
                    .stream()
                    .map(str -> TextHelper.Parsers.parsePlaceholderString(player, str))
                    .toList();

                commands.forEach(commandString -> CommandInteractiveInitializer.mimicCommandExecutionPacket((ServerPlayerEntity) player, commandString));
            }
        }

    }

    @Unique
    private static boolean canUseInteractiveCommand(PlayerEntity player) {
        return player.isSneaking();
    }

    @Unique
    public @NotNull String extractSignLines(@NotNull SignText signText) {
        return Arrays.stream(signText.getMessages(false))
                     .map(Text::getString)
                     .reduce("", String::concat);
    }

    @Unique
    /* text must contains "//" */
    public @NotNull List<String> splitCommands(@NotNull String text) {
        int left = text.indexOf(COMMAND_STRING_SPLIT_CHARACTER);

        // strip comments
        text = text.substring(left + 1);

        // split commands
        String[] split = text.split(COMMAND_STRING_SPLIT_CHARACTER);
        return Arrays.stream(split).map(String::trim).collect(Collectors.toCollection(ArrayList::new));
    }
}

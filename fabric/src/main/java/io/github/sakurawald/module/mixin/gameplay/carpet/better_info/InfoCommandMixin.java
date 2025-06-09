package io.github.sakurawald.module.mixin.gameplay.carpet.better_info;

import carpet.commands.InfoCommand;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InfoCommand.class)
public class InfoCommandMixin {

    @SuppressWarnings("DataFlowIssue")
    @Inject(method = "infoBlock", at = @At(value = "INVOKE", target = "Lcarpet/commands/InfoCommand;printBlock(Ljava/util/List;Lnet/minecraft/server/command/ServerCommandSource;Ljava/lang/String;)V", shift = At.Shift.AFTER))
    private static void addNbtQueryForInfoBlockCommand(@NotNull ServerCommandSource source, @NotNull BlockPos pos, String grep, CallbackInfoReturnable<Integer> cir) {
        // is player ?
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) return;

        // is block entity ?
        BlockEntity blockEntity = player.getWorld().getBlockEntity(pos);

        MutableText output = Text.empty().append("\n");
        if (blockEntity == null) {
            player.sendMessage(output.append(Text.literal("No block entity found at " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ())));
            return;
        }

        // send nbt data
        NbtCompound compoundTag = blockEntity.createNbtWithIdentifyingData(blockEntity.getWorld().getRegistryManager());
        player.sendMessage(output.append(Text.translatable("commands.data.block.query", pos.getX(), pos.getY(), pos.getZ(), NbtHelper.toPrettyPrintedText(compoundTag))));
    }
}

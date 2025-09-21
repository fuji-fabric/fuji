package mod.fuji.module.initializer.command_interactive;

import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.command.executor.CommandExecutor;
import mod.fuji.core.command.executor.structure.ExtendedCommandSource;
import mod.fuji.core.document.annotation.ColorBox;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.document.annotation.TestCase;
import mod.fuji.core.event.annotation.EventConsumer;
import mod.fuji.core.event.message.player.PlayerInteractBlockPreEvent;
import mod.fuji.module.initializer.ModuleInitializer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.block.AbstractSignBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.SignText;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

@Document(id = 1751824965598L, value = """
    This module allows `writing` commands on `sign blocks`, which can then be executed by `clicking` the sign.
    """)
@ColorBox(id = 1751870448041L, color = ColorBox.ColorBoxTypes.TIP, value = """
    ◉ How it works?

    If a player `right click` a `sign block`.
    We will check if the `facing texts` contains the `/` character.
    If contains, we will treat as the player issue the command.
    """)
@ColorBox(id = 1751968326784L, color = ColorBox.ColorBoxTypes.TIP, value = """
    A `sign block` that contains the `character /` is called an `interactive sign block`.
    You can use `right click` to execute the commands written on the interactive sign block.
    You can use `shift + right click` to edit an `interactive sign block`.
    """)
@ColorBox(id = 1751968409125L, color = ColorBox.ColorBoxTypes.TIP, value = """
    You can write some comment text before the first `character /`.
    All 4 lines will be joined and treated as one single big line.
    So be careful with the `space character`, and ignore the `linefeed character`.
    """)
@TestCase(action = "Test the `command_interactive` module in `online-mode` server.", targets = "The packet should not break the client-side signature validation.")
@TestCase(action = "Enable `command_warmup` module, issue `/back` command.", targets = "It should work with un-signed argument type.")
@TestCase(action = "Enable `command_warmup` module, issue `/say hi` command.", targets = "It should work with signed argument type.")
public class CommandInteractiveInitializer extends ModuleInitializer {

    private static final String COMMAND_STRING_SPLIT_CHARACTER = "/";

    private static @NotNull String mapSignTextIntoString(@NotNull SignText signText) {
        return Arrays.stream(signText.getMessages(false))
            .map(Text::getString)
            .reduce("", String::concat);
    }

    private static @NotNull List<String> splitCommands(@NotNull String string) {
        /* Remove leading comment string. */
        int left = string.indexOf(COMMAND_STRING_SPLIT_CHARACTER);
        string = string.substring(left + 1);

        /* Split commands. */
        String[] split = string.split(COMMAND_STRING_SPLIT_CHARACTER);
        return Arrays.stream(split).map(String::trim).collect(Collectors.toCollection(ArrayList::new));
    }

    private static boolean canUseInteractiveCommand(@NotNull ServerPlayerEntity player) {
        return player.isSneaking();
    }

    @EventConsumer(injectorPriority = EventConsumer.LOWER, consumerPriority = EventConsumer.LOWER)
    private static void consumePlayerInteractBlockPreEvent(PlayerInteractBlockPreEvent event) {
        if (event.getCallbackInfoReturnable().isCancelled()) return;

        World world = event.getWorld();
        BlockPos blockPos = event.getBlockHitResult().getBlockPos();
        BlockState blockState = world.getBlockState(blockPos);
        if (!(blockState.getBlock() instanceof AbstractSignBlock)) return;

        /* Check if the player can use interactive command now. */
        ServerPlayerEntity player = event.getPlayer();
        if (CommandInteractiveInitializer.canUseInteractiveCommand(player)) return;

        /* Extract the sign lines from the sign block. */
        BlockEntity interactingBlockEntity = world.getBlockEntity(blockPos);
        if (interactingBlockEntity instanceof SignBlockEntity signBlockEntity) {
            SignText facingSignText = signBlockEntity.getText(signBlockEntity.isPlayerFacingFront(player));
            String facingSignLines = CommandInteractiveInitializer.mapSignTextIntoString(facingSignText);
            if (facingSignLines.contains(CommandInteractiveInitializer.COMMAND_STRING_SPLIT_CHARACTER)) {
                /* Consume this interaction. */
                event.getCallbackInfoReturnable().setReturnValue(ActionResult.CONSUME);

                /* Execute commands as the player. */
                List<String> commands = CommandInteractiveInitializer.splitCommands(facingSignLines)
                    .stream()
                    .map(str -> TextHelper.Parsers.parsePlaceholderString(player, str))
                    .toList();
                commands.forEach(commandString -> {
                    CommandExecutor.executeSingle(ExtendedCommandSource.asPlayer(player.getCommandSource(), player), commandString);
                });
            }
        }
    }

}

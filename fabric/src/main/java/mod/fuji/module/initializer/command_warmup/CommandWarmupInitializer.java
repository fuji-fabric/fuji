package mod.fuji.module.initializer.command_warmup;

import mod.fuji.core.auxiliary.LogUtil;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.config.handler.abst.BaseConfigurationHandler;
import mod.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import mod.fuji.core.document.annotation.ColorBox;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.event.annotation.EventConsumer;
import mod.fuji.core.event.message.command.CommandExecutionPreEvent;
import mod.fuji.core.service.bossbar.BossBarManager;
import mod.fuji.core.service.bossbar.BossBarTicket;
import mod.fuji.core.structure.Tags;
import mod.fuji.module.initializer.ModuleInitializer;
import mod.fuji.module.initializer.command_warmup.config.model.CommandWarmupConfigModel;
import mod.fuji.module.initializer.command_warmup.config.transformer.CommandWarmupV1SchemaTransformer;
import mod.fuji.module.initializer.command_warmup.structure.CommandWarmupNode;
import mod.fuji.module.initializer.command_warmup.structure.CommandWarmupTicket;
import java.util.Optional;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Document(id = 1751826885335L, value = """
    This module allows defining a `cooldown` before command execution.
    """)
@ColorBox(id = 1751870580067L, color = ColorBox.ColorBoxTypes.NOTE, value = """
    The `command_warmup` module is `before` the command execution.
    The `command_cooldown` module is `after` the command execution.

    <blue>NOTE: Players with level permission 4 can bypass the command warmup.
    """)
@ColorBox(id = 1751974763151L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ Set up the warmup for all commands.
    Regex: `.+`

    The `.` character in regex means `match any character`.
    The `+` character in regex means `a quantifier, one or more times`.
    """)
@ColorBox(id = 1751974917420L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ Set up the warmup for all commands, but specify a special setup for `/back` command.
    You can just write the `config section` for `/back` command `at the top` of rules.
    The `rules` are `matched` from up to down, and `the first matched rule` will be used.
    So, you can just write the `special rule` at the top of other rules.
    """)
@ColorBox(id = 1751975050645L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ Set up the warmup for all command, except the `/back` command.
    Regex: `(?!back).+`

    The regex use the `negative lookahead`, to exclude the string that `starts with back`.
    """)
@ColorBox(id = 1751975123435L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ Share the same warmup setup for multiple commands.
    Regex: `(back)|(heal)|(feed)`

    You can use the `alternative character |` in `regex`.
    """)
public class CommandWarmupInitializer extends ModuleInitializer {
    private static final BaseConfigurationHandler<CommandWarmupConfigModel> config = ObjectConfigurationHandler
        .ofModule(BaseConfigurationHandler.CONFIG_JSON_LITERAL, CommandWarmupConfigModel.class)
        .installTransformer(new CommandWarmupV1SchemaTransformer());

    private static void processCommandWarmup(@NotNull ServerPlayerEntity player, @NotNull String commandString, @NotNull CallbackInfo callbackInfo) {
        LogUtil.debug("Process command warmup: player = {}, command = {}", PlayerHelper.getPlayerName(player), commandString);

        /* Iterate the node entries. */
        var config = CommandWarmupInitializer.config.model();
        for (CommandWarmupNode entry : config.rules) {

            /* Test if we should bypass this warmup entry. */
            if (Tags.hasAnyTagPermission(player, "command_warmup.bypass", entry.getTags())) {
                continue;
            }

            /* If a warmup entry matches the command string, then we cancel the usage of the command. */
            if (commandString.matches(entry.getCommand().getRegex())) {
                /* Should not send the warmup warning or cancel it, if the player can't even use that command. */
                if (!CommandHelper.Requirement.canUseCommandString(player, commandString)) {
                    break;
                }

                Optional<CommandWarmupTicket> commandWarmupTicket = BossBarManager.findBossbarTicket(CommandWarmupTicket.class, player);
                commandWarmupTicket
                    .filter(BossBarTicket::isCompleted)
                    .ifPresentOrElse(it -> {
                        // Now do the command execution.
                    }, () -> {
                        // Submit the command warmup ticket.
                        BossBarManager.addTicket(CommandWarmupTicket.make(player, commandString, entry));

                        // Send warning for movement.
                        if (config.warn_for_move) {
                            TextHelper.sendTextByKey(player, "command_warmup.warn_for_move", entry.getInterruptible().getInterruptDistance());
                        }

                        // Cancel the issue of command string.
                        callbackInfo.cancel();
                    });

                /* Apply the first matched rule. */
                break;
            }
        }
    }

    @EventConsumer(injectorPriority = EventConsumer.LOWEST, consumerPriority = EventConsumer.LOWEST)
    private static void consumeCommandExecutionPreEvent(CommandExecutionPreEvent event) {
        CallbackInfo callback = event.getCallback();
        if (callback.isCancelled()) return;
        ServerCommandSource commandSource = event.getCommandSource();
        if (CommandHelper.Source.isExecutedByConsole(commandSource)) return;
        if (config.model().admin_players_can_bypass_all_rules && CommandHelper.Requirement.isAdmin(commandSource)) return;

        CommandHelper.Source.withServerPlayerEntity(commandSource, player -> {
            processCommandWarmup(player, event.getCommandString(), callback);
        });
    }
}

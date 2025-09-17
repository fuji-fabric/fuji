package io.github.sakurawald.fuji.module.initializer.command_warmup;

import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.event.annotation.EventConsumer;
import io.github.sakurawald.fuji.core.event.message.command.CommandExecutionPreEvent;
import io.github.sakurawald.fuji.core.manager.impl.bossbar.BossBarManager;
import io.github.sakurawald.fuji.core.manager.impl.bossbar.BossBarTicket;
import io.github.sakurawald.fuji.core.structure.Tags;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.command_warmup.config.model.CommandWarmupConfigModel;
import io.github.sakurawald.fuji.module.initializer.command_warmup.config.transformer.CommandWarmupV1SchemaTransformer;
import io.github.sakurawald.fuji.module.initializer.command_warmup.structure.CommandWarmupNode;
import io.github.sakurawald.fuji.module.initializer.command_warmup.structure.CommandWarmupTicket;
import java.util.Optional;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Document(id = 1751826885335L, value = """
    This module allows you to define a `cooldown` before command execution.
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
@ColorBox(id = 1758083597322L, color = ColorBox.ColorBoxTypes.NOTE, value = """
    ◉ The `command warmup` will NOT be applied if...
    1. The command source is `the console`.
    2. The command source is `admin` (level permission >= 4)
    3. The command source has the `warmup bypass permission` for that command.
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
                if (!CommandHelper.Requirement.canUseThisCommand(player, commandString)) {
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
        if (CommandHelper.Requirement.isAdmin(commandSource)) return;

        CommandHelper.Source.withServerPlayerEntity(commandSource, player -> {
            processCommandWarmup(player, event.getCommandString(), callback);
        });
    }
}

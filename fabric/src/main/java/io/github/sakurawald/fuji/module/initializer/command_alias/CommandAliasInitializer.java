package io.github.sakurawald.fuji.module.initializer.command_alias;

import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.document.gui.CommandsInspectionGui;
import io.github.sakurawald.fuji.core.event.message.impl.CommandEvents;
import io.github.sakurawald.fuji.core.event.message.impl.ServerLifecycleEvents;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.command_alias.config.model.CommandAliasConfigModel;
import io.github.sakurawald.fuji.module.initializer.command_alias.service.CommandAliasService;
import io.github.sakurawald.fuji.module.initializer.command_alias.structure.AliasCommandDescriptor;
import net.minecraft.server.command.ServerCommandSource;

@Document(id = 1751826302190L, value = """
    This module allows you to define alias for existing commands.
    The defined alies command will redirect to the existing command node.
    """)
@ColorBox(id = 1751900420030L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ Define a `new shortcut command` as an alias to `an existing command`.
    For example:
    1. Define a new `/r` command, as alias to `/reply` command.
    2. Define a new `/sudo` command, as alias to `/run as fake-op` command.
    """)
@ColorBox(id = 1751900579678L, color = ColorBox.ColorBoxTypes.TIPS, value = """
    The `command_alias` command only has the ability to define `a new command`, as the alias of `an existing command`.
    It works by `command redirecting`.
    You can't use `command_alias` module to actually define `a new command with arguments`, that's the weakness.
    If you want to define `a new command with arguments`, see `command_bundle` module.

    In short:
    1. To define `a simple command` with no arguments, use `command_alias` module.
    2. To define `a complex command` with arguments, use `command_bundle` module.
    """)
@CommandNode("command-alias")
@CommandRequirement(level = 4)
public class CommandAliasInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<CommandAliasConfigModel> config = ObjectConfigurationHandler.ofModule(BaseConfigurationHandler.CONFIG_JSON_LITERAL, CommandAliasConfigModel.class);

    @Document(id = 1756022056042L, value = "List all registered alias-commands in server.")
    @CommandNode("list")
    private static int $list(@CommandSource CommandContext<ServerCommandSource> ctx) {
        return CommandsInspectionGui
            .inspectCommandDescriptors(ctx, it -> it instanceof AliasCommandDescriptor);
    }

    @Override
    protected void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
            CommandAliasService.registerAllAliasCommands();
            CommandEvents.REGISTRATION.register((a, b, c) -> CommandAliasService.registerAllAliasCommands());
        });
    }

    @Override
    protected void onReload() {
        CommandAliasService.unregisterAllAliasCommands();
        CommandAliasService.registerAllAliasCommands();
    }

}

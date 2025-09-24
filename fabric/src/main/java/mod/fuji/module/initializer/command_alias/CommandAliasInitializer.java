package mod.fuji.module.initializer.command_alias;

import com.mojang.brigadier.context.CommandContext;
import mod.fuji.core.annotation.Unused;
import mod.fuji.core.auxiliary.minecraft.ServerHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandRequirement;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.core.config.handler.abst.BaseConfigurationHandler;
import mod.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import mod.fuji.core.document.annotation.ColorBox;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.document.gui.CommandsInspectionGui;
import mod.fuji.core.event.annotation.EventConsumer;
import mod.fuji.core.event.message.server.command.CommandRegistrationEvent;
import mod.fuji.core.event.message.server.lifecycle.ServerStartedEvent;
import mod.fuji.module.initializer.ModuleInitializer;
import mod.fuji.module.initializer.command_alias.config.model.CommandAliasConfigModel;
import mod.fuji.module.initializer.command_alias.service.CommandAliasService;
import mod.fuji.module.initializer.command_alias.structure.AliasCommandDescriptor;
import net.minecraft.server.command.ServerCommandSource;

@Document(id = 1751826302190L, value = """
    This module allows defining `aliases` for `an existing target command`.
    The defined `alias command` redirects to the corresponding existing command node.
    """)
@ColorBox(id = 1751900420030L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ Define a `new shortcut command` as an alias to `an existing command`.
    For example:
    1. Define a new `/r` command, as alias to `/reply` command.
    2. Define a new `/sudo` command, as alias to `/run as fake-op` command.
    """)
@ColorBox(id = 1751900579678L, color = ColorBox.ColorBoxTypes.TIP, value = """
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

    @EventConsumer
    private static void registerAllAliasCommands(@Unused ServerStartedEvent event) {
        CommandAliasService.registerAllAliasCommands();
    }

    @EventConsumer(injectorPriority = EventConsumer.HIGHER)
    private static void registerAllAliasCommands(@Unused CommandRegistrationEvent event) {
        ServerHelper.Lifecycle.withServerInstantiated(CommandAliasService::registerAllAliasCommands);
    }

    @Override
    protected void onReload() {
        CommandAliasService.unregisterAllAliasCommands();
        CommandAliasService.registerAllAliasCommands();
    }

}

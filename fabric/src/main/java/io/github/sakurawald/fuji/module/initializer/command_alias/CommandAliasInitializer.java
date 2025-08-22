package io.github.sakurawald.fuji.module.initializer.command_alias;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.event.impl.ServerLifecycleEvents;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.command_alias.config.model.CommandAliasConfigModel;
import io.github.sakurawald.fuji.module.initializer.command_alias.structure.CommandPathMappingNode;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.NotNull;

import java.util.List;

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

public class CommandAliasInitializer extends ModuleInitializer {

    private static final BaseConfigurationHandler<CommandAliasConfigModel> config = ObjectConfigurationHandler.ofModule(BaseConfigurationHandler.CONFIG_JSON_LITERAL, CommandAliasConfigModel.class);

    @Override
    protected void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
            CommandDispatcher<ServerCommandSource> dispatcher = CommandHelper.getCommandDispatcher();
            config.model().alias.forEach(it -> {
                assert dispatcher != null;
                processCommandAliasEntry(dispatcher, it);
            });
        });
    }

    private void processCommandAliasEntry(@NotNull CommandDispatcher<ServerCommandSource> dispatcher, @NotNull CommandPathMappingNode entry) {
        /* build the command node */
        LiteralArgumentBuilder<ServerCommandSource> builder = null;
        CommandNode<ServerCommandSource> target = dispatcher.findNode(entry.getTo());
        for (int i = entry.getFrom().size() - 1; i >= 0; i--) {
            String name = entry.getFrom().get(i);

            if (builder == null) {
                if (target == null) {
                    LogUtil.warn("Can't find the target command node for command alias entry: {}", entry);
                    return;
                }

                builder = CommandManager.literal(name).redirect(target);
                continue;
            }

            builder = CommandManager.literal(name).then(builder);
        }
        if (builder == null) return;

        /* copy the requirement from the parent of the target node */
        CommandNode<ServerCommandSource> targetRoot = dispatcher.findNode(List.of(entry.getTo().get(0)));
        if (targetRoot != null) {
            builder.requires(targetRoot.getRequirement());
        }

        /* register the command node */
        dispatcher.register(builder);
    }
}

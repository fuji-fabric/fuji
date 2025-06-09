package io.github.sakurawald.module.initializer.command_alias;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import io.github.sakurawald.core.auxiliary.LogUtil;
import io.github.sakurawald.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.core.event.impl.ServerLifecycleEvents;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import io.github.sakurawald.module.initializer.command_alias.config.model.CommandAliasConfigModel;
import io.github.sakurawald.module.initializer.command_alias.structure.CommandPathMappingNode;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CommandAliasInitializer extends ModuleInitializer {

    private static final BaseConfigurationHandler<CommandAliasConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, CommandAliasConfigModel.class);

    @Override
    protected void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
            CommandDispatcher<ServerCommandSource> dispatcher = ServerHelper.getCommandDispatcher();
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
                    LogUtil.warn("can't find the target command node for command alias entry: {}", entry);
                    return;
                }

                builder = CommandManager.literal(name).redirect(target);
                continue;
            }

            builder = CommandManager.literal(name).then(builder);
        }
        if (builder == null) return;

        /* copy the requirement from the parent of the target node */
        CommandNode<ServerCommandSource> targetRoot = dispatcher.findNode(List.of(entry.getTo().getFirst()));
        if (targetRoot != null) {
            builder.requires(targetRoot.getRequirement());
        }

        /* register the command node */
        dispatcher.register(builder);
    }
}

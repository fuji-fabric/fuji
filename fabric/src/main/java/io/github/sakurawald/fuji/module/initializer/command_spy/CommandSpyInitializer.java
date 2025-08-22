package io.github.sakurawald.fuji.module.initializer.command_spy;

import com.mojang.brigadier.ParseResults;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.command_spy.config.model.CommandSpyConfigModel;
import net.minecraft.server.command.ServerCommandSource;

@Document(id = 1751826800901L, value = """
    This module logs the `issued commands` into the console.
    """)
public class CommandSpyInitializer extends ModuleInitializer {
    public static final BaseConfigurationHandler<CommandSpyConfigModel> config = ObjectConfigurationHandler.ofModule(BaseConfigurationHandler.CONFIG_JSON_LITERAL, CommandSpyConfigModel.class);

    public static void processCommandSpy(ParseResults<ServerCommandSource> parseResults) {
        /* Verify command source. */
        ServerCommandSource source = parseResults.getContext().getSource();
        if (!CommandSpyInitializer.config.model().spy_on_console
            && source.getPlayer() == null) return;

        /* Should not spy on ignored_commands. */
        String name = source.getName();
        String string = parseResults.getReader().getString();

        if (config.model()
            .ignore_commands
            .stream()
            .anyMatch(it -> {
                boolean flag = string.matches(it);
                if (flag) {
                    LogUtil.info("{} issued the ignored command: /{}", name, it);
                }
                return flag;
            })) {
            return;
        }

        /* Should we only spy on specified commands? */
        if (config.model()
            .only_spy_these_commands
            .enable) {

            boolean matchesAnyCommandThatOnlySpies = config.model()
                .only_spy_these_commands.commands
                .stream()
                .anyMatch(string::matches);

            if (!matchesAnyCommandThatOnlySpies) {
                return;
            }
        }

        /* Simply spy on all commands. */
        LogUtil.info("{} issued the server command: /{}", name, string);
    }
}

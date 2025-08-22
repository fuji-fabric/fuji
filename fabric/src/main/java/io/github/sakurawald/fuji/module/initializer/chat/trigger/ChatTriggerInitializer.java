package io.github.sakurawald.fuji.module.initializer.chat.trigger;

import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.StringUtil;
import io.github.sakurawald.fuji.core.command.executor.CommandExecutor;
import io.github.sakurawald.fuji.core.command.executor.structure.ExtendedCommandSource;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.chat.trigger.config.model.ChatTriggerConfigModel;
import java.util.List;
import java.util.regex.Matcher;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.NotNull;

@Document(id = 1751826730890L, value = """
    This module allows you to define magic spells in chat, to execute commands.
    """)
@ColorBox(id = 1754612494551L, color = ColorBox.ColorBoxTypes.TIPS, value = """
    ◉ Use `chat.trigger` to create `chat commands`.
    The `regex` in `chat.replace` is used to `find` the target pattern in `the given string`.
    The `regex` in `chat.trigger` is used to `match` the target pattern against `the given string`.

    You can define a `chat command` like `!report` or `!noclip` in other games.
    """)
@ColorBox(id = 1751899049909L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ To define a simple magic spell in chat string.
    Regex: `magic`
    Commands: `say magic!`
    """)
@ColorBox(id = 1751899198263L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ To define a complex magic spell with arguments in chat string.
    Regex: `i am (.+)`
    Commands:
    1. `say You just said: $0`
    2. `say Hello $1`
    """)
@ColorBox(id = 1751899201560L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ To define a shortcut for chat display module with Styled Chat mod.
    Regex: `(?<=^|\\\\s)item(?=\\\\s|$)`
    Commands: `run as fake-op %player:name% chat display item`
    """)
public class ChatTriggerInitializer extends ModuleInitializer {

    private static final BaseConfigurationHandler<ChatTriggerConfigModel> config = ObjectConfigurationHandler.ofModule(BaseConfigurationHandler.CONFIG_JSON_LITERAL, ChatTriggerConfigModel.class);

    public static void processChatTriggers(@NotNull ServerCommandSource source, @NotNull String chatString) {
        LogUtil.debug("Process chat triggers for input: chatString = {}", chatString);

        /* Enumerate triggers. */
        config.model()
            .getTriggers()
            .stream()
            .filter(it -> it.getCachedPattern().matcher(chatString).matches())
            .forEach(chatTrigger -> {
                /* Initialize the matcher. */
                Matcher matcher = chatTrigger.getCachedPattern().matcher(chatString);

                /* Replace the captured groups for commands. */
                List<String> commands = chatTrigger.getCommands()
                    .stream()
                    .map(cmd -> StringUtil.replaceAllAndResetMatcher(matcher, cmd))
                    .toList();

                /* Execute commands. */
                CommandExecutor.execute(ExtendedCommandSource.asConsole(source), commands);
            });
    }

}

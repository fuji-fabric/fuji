package io.github.sakurawald.fuji.module.initializer.command_rewrite;

import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.document.annotation.TestCase;
import io.github.sakurawald.fuji.core.structure.RegexRewriteNode;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.command_rewrite.config.model.CommandRewriteConfigModel;
import org.jetbrains.annotations.NotNull;

@Document(id = 1751826288031L, value = """
    This module allows you to define `regex` to rewrite the command line a player issued.
    """)
@ColorBox(id = 1751971914915L, color = ColorBox.ColorBoxTypes.TIPS, value = """
    ◉ You can use this module to shorten the command string.
    Like, rewrite the command string `/home` into `/home tp default`.
    And provide a shortcut, for `/home tp default` command.
    """)
@ColorBox(id = 1751971980398L, color = ColorBox.ColorBoxTypes.NOTE, value = """
    ◉ The different compared to `command_alias` module and `command_bundle` module.
    1. The `command alias` module allows you to define `new command`, and redirects it into `an existing command`.
    2. The `command bundle` module allows you to define `new command`, and define the argument and body of the new command.
    3. The `command rewrite` module didn't define any new command. It just works in `network packet` level, and modifies the `issued command string` from the command source player.
    """)
@TestCase(action = "Issue `/home` command.", targets = "It should be rewrite to `/home tp default` command.")
public class CommandRewriteInitializer extends ModuleInitializer {
    public static final BaseConfigurationHandler<CommandRewriteConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON_LITERAL, CommandRewriteConfigModel.class);

    public static String rewriteCommand(@NotNull String oldString) {
        for (RegexRewriteNode entry : CommandRewriteInitializer.config.model().rules) {
            if (entry.getRegex() == null || entry.getReplacement() == null) {
                LogUtil.warn("There is an invalid `null` entry in `command_rewrite.regex`, you should remove it: {}", entry);
                continue;
            }

            if (oldString.matches(entry.getRegex())) {
                String newString = oldString.replaceAll(entry.getRegex(), entry.getReplacement());
                LogUtil.debug("Rewrite the command: old = {}, new = {}", oldString, newString);
                return newString;
            }
        }

        return oldString;
    }
}

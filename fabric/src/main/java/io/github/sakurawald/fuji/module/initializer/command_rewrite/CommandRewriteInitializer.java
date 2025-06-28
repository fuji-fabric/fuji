package io.github.sakurawald.fuji.module.initializer.command_rewrite;

import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.structure.RegexRewriteNode;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.command_rewrite.config.model.CommandRewriteConfigModel;
import org.jetbrains.annotations.NotNull;

@Document("""
    This module allows you to define `regex` to rewrite the command line a player issued.
    """)
public class CommandRewriteInitializer extends ModuleInitializer {
    public static final BaseConfigurationHandler<CommandRewriteConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, CommandRewriteConfigModel.class);

    public static String rewriteCommand(@NotNull String oldString) {
        for (RegexRewriteNode entry : CommandRewriteInitializer.config.model().rewrite) {
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

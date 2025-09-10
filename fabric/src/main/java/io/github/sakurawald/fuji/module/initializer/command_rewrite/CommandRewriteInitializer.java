package io.github.sakurawald.fuji.module.initializer.command_rewrite;

import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.document.annotation.TestCase;
import io.github.sakurawald.fuji.core.event.annotation.EventConsumer;
import io.github.sakurawald.fuji.core.event.message.player.PlayerCommandIssuePreEvent;
import io.github.sakurawald.fuji.core.structure.RegexRewriteNode;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.command_rewrite.config.model.CommandRewriteConfigModel;
import java.util.List;
import org.jetbrains.annotations.NotNull;

@Document(id = 1751826288031L, value = """
    This module allows you to define `regex` to rewrite the command line a player issued.
    """)
@ColorBox(id = 1756047806466L, color = ColorBox.ColorBoxTypes.NOTE, value = """
    ◉ How it works?
    1. Intercept the `issued command string` packets.
    2. Apply the defined rewrite rules to `the command string` in top-down order.

    <blue>NOTE: The `command_rewrite` occurs at the very beginning of the command lifecycle.
    <blue>You will not receive `command suggestions` or `command exceptions` at this stage.
    <blue>The rewrite rules are applied directly to the raw command string.

    ◉ Command Lifecycle
    1. **Issuing** – The command is sent as `a raw string`. At this stage, no command suggestions or exceptions are available.
    2. **Parsing** – The command string is parsed into `a command node`. At this point, command suggestions and exceptions can be provided.
    3. **Execution** – The `actions` associated with the command node are `executed`, producing the intended `effects` of the command.
    """)
@ColorBox(id = 1751971980398L, color = ColorBox.ColorBoxTypes.NOTE, value = """
    ◉ The different compared to `command_alias` module and `command_bundle` module.
    1. The `command alias` module allows you to define `new command`, and redirects it into `an existing command`.
    2. The `command bundle` module allows you to define `new command`, and define the argument and body of the new command.
    3. The `command rewrite` module didn't define any new command. It just works in `network packet` level, and modifies the `issued command string` from the command source player.
    """)
@ColorBox(id = 1751971914915L, color = ColorBox.ColorBoxTypes.TIPS, value = """
    ◉ You can use this module to shorten the command string.
    Like, rewrite the command string `/home` into `/home tp default`.
    And provide a shortcut, for `/home tp default` command.
    """)
@TestCase(action = "Issue `/home` command.", targets = "It should be rewrite to `/home tp default` command.")
public class CommandRewriteInitializer extends ModuleInitializer {
    private static final BaseConfigurationHandler<CommandRewriteConfigModel> config = ObjectConfigurationHandler.ofModule(BaseConfigurationHandler.CONFIG_JSON_LITERAL, CommandRewriteConfigModel.class);

    private static @NotNull String processCommandRewrite(@NotNull String oldString) {
        /* Compute effective rewrite rules. */
        List<RegexRewriteNode> effectiveRewriteRules = CommandRewriteInitializer.config.model().rules
            .stream()
            .filter(rule -> {
                boolean result = rule.getRegex() != null && rule.getReplacement() != null;
                if (!result) {
                    LogUtil.warn("Both regex property and replacement property should not be null, ignoring the command rewrite rule: {}", rule);
                }
                return result;
            })
            .toList();

        /* Applied the rewrite rules. */
        for (RegexRewriteNode rewriteRule : effectiveRewriteRules) {
            if (oldString.matches(rewriteRule.getRegex())) {
                String newString = oldString.replaceAll(rewriteRule.getRegex(), rewriteRule.getReplacement());
                LogUtil.debug("Rewrite the command string: old = {}, new = {}", oldString, newString);
                oldString = newString;
            }
        }

        return oldString;
    }

    @EventConsumer(injectorPriority = EventConsumer.LOWEST, consumerPriority = EventConsumer.LOWEST)
    private static void consumePlayerCommandIssuePreEvent(PlayerCommandIssuePreEvent event) {
        if (event.getCallbackInfo().isCancelled()) return;

        String oldValue = event.getCommandString();
        String newValue = CommandRewriteInitializer.processCommandRewrite(oldValue);
        event.setCommandString(newValue);
    }
}

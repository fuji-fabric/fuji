package mod.fuji.module.initializer.command_meta.shell;

import com.mojang.brigadier.context.CommandContext;
import mod.fuji.core.auxiliary.AsyncUtil;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.auxiliary.LogUtil;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandRequirement;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.core.command.argument.wrapper.impl.GreedyString;
import mod.fuji.core.command.exception.AbortCommandExecutionException;
import mod.fuji.core.config.handler.abst.BaseConfigurationHandler;
import mod.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import mod.fuji.core.document.annotation.ColorBox;
import mod.fuji.module.initializer.ModuleInitializer;
import mod.fuji.module.initializer.command_meta.shell.config.ShellConfigModel;
import java.nio.charset.StandardCharsets;
import lombok.Cleanup;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@Document(id = 1751824777515L, value = """
    Provides `/shell` command.
    To execute the `command line` in `host shell`.

    A `powerful` and `dangerous` module.
    """)
@ColorBox(id = 1751870434188L, color = ColorBox.ColorBoxTypes.DANGER, value = """
    ◉ This is a `dangerous` module.
    This module is a powerful and dangerous module, not recommended to enable it.
    """)
@ColorBox(id = 1751970272178L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ Create a file using placeholder.
    Issue: `/shell touch %player:name%.dangerous`

    ◉ Execute a program in the host machine.
    Issue: `/shell emacs`

    ◉ Call a program in the host machine, to backup your server.
    You need to combine `shell` module with `command_scheduler` module.
    And setup the `external backup program`.
    See more in https://rdiff-backup.net/

    ◉ Download a virus from the Internet, and execute it.
    Issue: `/shell ...`
    """)



public class ShellInitializer extends ModuleInitializer {

    private static final BaseConfigurationHandler<ShellConfigModel> config = ObjectConfigurationHandler.ofModule(BaseConfigurationHandler.CONFIG_JSON_LITERAL, ShellConfigModel.class);

    private static void checkSecurity(CommandContext<ServerCommandSource> ctx) {
        var config = ShellInitializer.config.model();

        if (!config.enable_warning.equals("CONFIRM")) {
            TextHelper.sendTextByKey(ctx.getSource(), "shell.failed.rtfm");
            throw new AbortCommandExecutionException();
        }

        if (config.security.only_allow_console && ctx.getSource().getPlayer() != null) {
            TextHelper.sendTextByKey(ctx.getSource(), "command.console_only");
            throw new AbortCommandExecutionException();
        }

        if (ctx.getSource().getName() != null && !config.security.allowed_player_names.contains(ctx.getSource().getName())) {
            TextHelper.sendTextByKey(ctx.getSource(), "shell.failed.not_in_allowed_list");
            throw new AbortCommandExecutionException();
        }

    }

    @Document(id = 1751824784016L, value = "Execute a shell command in host os.")
    @CommandNode("shell")
    @CommandRequirement(level = 4)
    private static int $shell(@CommandSource CommandContext<ServerCommandSource> ctx, GreedyString rest) {
        checkSecurity(ctx);

        String commandString = rest.getValue();
        AsyncUtil.runAsyncAndHandleExceptions(() -> {
            try {
                LogUtil.info("Shell exec: {}", commandString);

                /* Call the shell. */
                String[] commandTokens = commandString.split("\\s");
                Process process = Runtime.getRuntime().exec(commandTokens, null, null);
                InputStream inputStream = process.getInputStream();
                @Cleanup BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                StringBuilder output = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
                process.waitFor();

                // Send feedback.
                LogUtil.info(output.toString());
                TextHelper.sendMessageByText(ctx.getSource(), Text.literal(output.toString()));
            } catch (IOException | InterruptedException e) {
                LogUtil.error("Failed to execute the shell command: {}", commandString, e);
            }
        });

        // The return value of shell command is always treated as SUCCESS.
        return CommandHelper.Return.SUCCESS;
    }
}

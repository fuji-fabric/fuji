package io.github.sakurawald.module.initializer.command_meta.shell;

import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.core.annotation.Document;
import io.github.sakurawald.core.auxiliary.LogUtil;
import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.command.annotation.CommandNode;
import io.github.sakurawald.core.command.annotation.CommandRequirement;
import io.github.sakurawald.core.command.annotation.CommandSource;
import io.github.sakurawald.core.command.argument.wrapper.impl.GreedyString;
import io.github.sakurawald.core.command.exception.AbortCommandExecutionException;
import io.github.sakurawald.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import io.github.sakurawald.module.initializer.command_meta.shell.config.ShellConfigModel;
import lombok.Cleanup;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.CompletableFuture;

public class ShellInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<ShellConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, ShellConfigModel.class);

    private static void checkSecurity(CommandContext<ServerCommandSource> ctx) {
        var config = ShellInitializer.config.model();

        if (!config.enable_warning.equals("CONFIRM")) {
            TextHelper.sendMessageByKey(ctx.getSource(), "shell.failed.rtfm");
            throw new AbortCommandExecutionException();
        }

        if (config.security.only_allow_console && ctx.getSource().getPlayer() != null) {
            TextHelper.sendMessageByKey(ctx.getSource(), "command.console_only");
            throw new AbortCommandExecutionException();
        }

        if (ctx.getSource().getName() != null && !config.security.allowed_player_names.contains(ctx.getSource().getName())) {
            TextHelper.sendMessageByKey(ctx.getSource(), "shell.failed.not_in_allowed_list");
            throw new AbortCommandExecutionException();
        }

    }

    @SuppressWarnings("deprecation")
    @CommandNode("shell")
    @CommandRequirement(level = 4)
    @Document("Execute a shell command in host os.")
    private static int shell(@CommandSource CommandContext<ServerCommandSource> ctx, GreedyString rest) {
        checkSecurity(ctx);

        String $rest = rest.getValue();
        CompletableFuture.runAsync(() -> {
            try {
                LogUtil.info("shell exec: {}", $rest);

                Process process = Runtime.getRuntime().exec($rest, null, null);
                InputStream inputStream = process.getInputStream();
                @Cleanup BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder output = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
                process.waitFor();

                // output
                LogUtil.info(output.toString());
                ctx.getSource().sendMessage(Text.literal(output.toString()));
            } catch (IOException | InterruptedException e) {
                LogUtil.error("failed to execute a shell command.", e);
            }
        });

        return CommandHelper.Return.SUCCESS;
    }
}

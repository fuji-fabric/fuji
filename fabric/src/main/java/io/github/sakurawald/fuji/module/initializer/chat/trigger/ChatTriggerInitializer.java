package io.github.sakurawald.fuji.module.initializer.chat.trigger;

import io.github.sakurawald.fuji.core.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.StringUtil;
import io.github.sakurawald.fuji.core.command.executor.CommandExecutor;
import io.github.sakurawald.fuji.core.command.structure.ExtendedCommandSource;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.chat.trigger.config.model.ChatTriggerConfigModel;
import io.github.sakurawald.fuji.module.initializer.chat.trigger.structure.ChatTrigger;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Document("""
    This module allows you to define magic spells in chat, to execute commands.
    """)
public class ChatTriggerInitializer extends ModuleInitializer {

    private static final BaseConfigurationHandler<ChatTriggerConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, ChatTriggerConfigModel.class);

    @SuppressWarnings("DuplicatedCode")
    public static String processChatTriggers(ServerCommandSource source, String chatString, CallbackInfo ci) {
        /* Log it. */
        LogUtil.debug("Process Chat Triggers: chatString = {}", chatString);

        /* Enumerate triggers. */
        List<ChatTrigger> triggers = config.model().triggers;
        triggers
            .stream()
            .filter(it -> chatString.matches(it.regex))
            .forEach(it -> {
                /* Replace captured-groups for commands. */
                Matcher matcher = Pattern
                    .compile(it.regex)
                    .matcher(chatString);
                matcher.find();
                List<String> commands = it.commands
                    .stream()
                    .map(cmd -> StringUtil.replaceGroupsPlaceholders(matcher, cmd))
                    .collect(Collectors.toCollection(ArrayList::new));

                /* Execute commands. */
                LogUtil.debug("Execute commands {} for {}", commands, it);
                commands.forEach(cmd -> CommandExecutor.execute(ExtendedCommandSource.asConsole(source), cmd));
            });

        return chatString;
    }

}

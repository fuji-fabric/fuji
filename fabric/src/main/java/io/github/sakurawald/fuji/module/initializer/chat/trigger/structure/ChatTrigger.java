package io.github.sakurawald.fuji.module.initializer.chat.trigger.structure;

import java.util.regex.Pattern;
import lombok.Data;

import java.util.List;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

@Data
public class ChatTrigger {

    public String regex;
    public List<String> commands;

    public static ChatTrigger make(@NotNull String regex, @NotNull List<String> commands) {
        ChatTrigger chatTrigger = new ChatTrigger();
        chatTrigger.regex = regex;
        chatTrigger.commands = commands;
        return chatTrigger;
    }

    @ToString.Exclude
    transient Pattern pattern;

    public Pattern getCompiledRegex() {
        if (this.pattern == null) {
            this.pattern = Pattern.compile(this.regex);
        }

        return this.pattern;
    }

}

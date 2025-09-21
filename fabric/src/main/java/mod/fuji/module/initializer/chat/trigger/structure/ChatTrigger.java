package mod.fuji.module.initializer.chat.trigger.structure;

import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.Data;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

@Data
@NoArgsConstructor
public class ChatTrigger {

    String regex;
    List<String> commands;

    public static ChatTrigger make(@NotNull String regex, @NotNull List<String> commands) {
        ChatTrigger chatTrigger = new ChatTrigger();
        chatTrigger.regex = regex;
        chatTrigger.commands = commands;
        return chatTrigger;
    }

    @ToString.Exclude
    @Getter(AccessLevel.NONE)
    transient Pattern pattern;

    public Pattern getCachedPattern() {
        if (this.pattern == null) {
            this.pattern = Pattern.compile(this.regex);
        }

        return this.pattern;
    }

}

package mod.fuji.module.initializer.chat.trigger.structure;

import com.google.gson.annotations.SerializedName;
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

    @SerializedName(value = "chat_string_regex", alternate = "regex")
    String chatStringRegex;
    List<String> commands;

    public static ChatTrigger make(@NotNull String regex, @NotNull List<String> commands) {
        ChatTrigger chatTrigger = new ChatTrigger();
        chatTrigger.chatStringRegex = regex;
        chatTrigger.commands = commands;
        return chatTrigger;
    }

    @ToString.Exclude
    @Getter(AccessLevel.NONE)
    transient Pattern pattern;

    public Pattern getCachedPattern() {
        if (this.pattern == null) {
            this.pattern = Pattern.compile(this.chatStringRegex);
        }

        return this.pattern;
    }

}

package mod.fuji.module.initializer.command_advice.structure;

import mod.fuji.core.document.annotation.Document;
import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommandAdviceEntry {

    boolean enable = true;

    @Nullable String document = null;

    Matcher matcher = new Matcher();
    @Data
    @NoArgsConstructor
    public static class Matcher {

        @Document(id = 1751826314407L, value = """
        The `regex` expression used to match the `target command`.
        """)
        String commandStringRegex;

        @Document(id = 1751826318098L, value = """
        Is this `advice` only valid, when the target command is executed by a player?
        """)
        boolean acceptPlayerCommandSource = true;
        boolean acceptConsoleCommandSource;

        public Matcher(String commandStringRegex, boolean acceptPlayerCommandSource, boolean acceptConsoleCommandSource) {
            this.commandStringRegex = commandStringRegex;
            this.acceptPlayerCommandSource = acceptPlayerCommandSource;
            this.acceptConsoleCommandSource = acceptConsoleCommandSource;
        }

        @ToString.Exclude
        @Getter(AccessLevel.NONE)
        transient Pattern pattern;
        public Pattern getCachedPattern() {
            if (this.pattern == null) {
                this.pattern = Pattern.compile(this.commandStringRegex);
            }

            return this.pattern;
        }

    }

    @Document(id = 1751826320651L, value = """
        The type of this advice.
        """)
    CommandAdviceType adviceType;

    @Document(id = 1751826322556L, value = """
        The commands to execute when `perform` this `advice`.
        """)
    List<String> commands;

}

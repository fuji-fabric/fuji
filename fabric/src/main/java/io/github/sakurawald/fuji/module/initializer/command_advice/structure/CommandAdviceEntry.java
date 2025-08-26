package io.github.sakurawald.fuji.module.initializer.command_advice.structure;

import io.github.sakurawald.fuji.core.document.annotation.Document;
import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommandAdviceEntry {

    boolean enable = true;

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
        boolean executedByPlayerOnly;

        public Matcher(String commandStringRegex, boolean executedByPlayerOnly) {
            this.commandStringRegex = commandStringRegex;
            this.executedByPlayerOnly = executedByPlayerOnly;
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

    public boolean isCancellableAdviceType() {
        return this.getAdviceType().isCancellable();
    }
}

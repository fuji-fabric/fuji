package mod.fuji.module.initializer.command_spy.structure;

import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Nullable;

@Data
@NoArgsConstructor
public class CommandSpyRule {

    boolean enable;

    @Nullable String document;

    Matcher matcher = new Matcher();

    @Data
    @NoArgsConstructor
    public static class Matcher {
        String commandStringRegex = ".+";

        @Getter(AccessLevel.NONE)
        transient Pattern pattern;
        public Pattern getCachedPattern() {
            if (this.pattern == null) {
                this.pattern = Pattern.compile(this.commandStringRegex);
            }

            return this.pattern;
        }

        boolean acceptSilentCommand = false;
        boolean acceptPlayerCommandSource = true;
        boolean acceptServerCommandSource = false;
    }

    IfMatched ifMatched = new IfMatched();

    @Data
    @NoArgsConstructor
    public static class IfMatched {
        boolean logToConsole = true;
        int notifyPlayersWithLevelPermission = 4 + 1;
    }

}

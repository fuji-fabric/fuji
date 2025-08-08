package io.github.sakurawald.fuji.module.initializer.warning.config.model;

import com.google.gson.annotations.SerializedName;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.module.initializer.warning.structure.WarningRule;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class WarningConfigModel {

    @Document(id = 1754643821825L, value = "Should we send the reminders to the warned player?")
    WarningReminder warningReminder = new WarningReminder();

    @Data
    @NoArgsConstructor
    public static class WarningReminder {
        boolean remindWarnedPlayerOnJoinServer = true;

        ReminderSource reminderSource = new ReminderSource();
        @Data
        @NoArgsConstructor
        public static class ReminderSource {
            boolean remindPermanentWarningsType = false;
            boolean remindTemporalWarningsType = true;
        }
    }

    @Document(id = 1751827028093L, value = """
        Define `warning rules`, to execute `punishment commands`.

        When a new `warning` is `added` to a player, we will process the `warning rules`.
        And then pick up `one warning rule` to execute its commands.
        We will pick the `highest` number of warnings satisfied first.

        """)
    @SerializedName(value = "on_permanent_warning_created", alternate = "rules")
    List<WarningRule> onPermanentWarningCreated = new ArrayList<>() {
        {
            this.add(WarningRule.make(1, List.of(
                "send-broadcast <dark_red>Player %player:name% has just received a permanent-warning.<newline><dark_red>◉ Reason: %fuji:last_warning_reason%"
                , "when-online %player:name% send-message %player:name% <dark_red>You have received a warning.")));
            this.add(WarningRule.make(3, List.of(
                "temp-ban player %player:name% 30m Warned 3 times.")));

        }
    };

    List<WarningRule> onTemporalWarningCreated = new ArrayList<>() {
        {
            this.add(WarningRule.make(1, List.of(
                "send-broadcast <dark_red>Player %player:name% has just received a temporal-warning.<newline><dark_red>◉ Expiration Date: %fuji:last_warning_expiration_date%<newline><dark_red>◉ Reason: %fuji:last_warning_reason%"
            )));

            this.add(WarningRule.make(3, List.of(
                "send-broadcast <dark_red>Player %player:name% has just received a temporal-warning.<newline><dark_red>◉ Expiration Date: %fuji:last_warning_expiration_date%<newline><dark_red>◉ Reason: %fuji:last_warning_reason%"
                , "warning create %player:name% Received too many temporal warnings in a short period of time."
            )));

        }
    };

}

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
                "send-broadcast <dark_red>Player %player:name% just receive a permanent-warning."
                , "when-online %player:name% send-message %player:name% <red>You are warned. Watch your behaviour!")));
            this.add(WarningRule.make(3, List.of(
                "temp-ban player %player:name% 30m Warned 3 times.")));

        }
    };

    List<WarningRule> onTemporalWarningCreated = new ArrayList<>() {
        {
            this.add(WarningRule.make(1, List.of(
                "send-broadcast <dark_red>Player %player:name% just receive a temporal-warning."
            )));

        }
    };

}

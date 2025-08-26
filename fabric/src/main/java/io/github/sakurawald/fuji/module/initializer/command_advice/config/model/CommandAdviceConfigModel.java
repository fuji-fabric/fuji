package io.github.sakurawald.fuji.module.initializer.command_advice.config.model;

import com.google.gson.annotations.SerializedName;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.module.initializer.command_advice.structure.CommandAdviceEntry;
import io.github.sakurawald.fuji.module.initializer.command_advice.structure.CommandAdviceType;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CommandAdviceConfigModel {

    @Document(id = 1751826311892L, value = """
        Define `advices` for the `target command`.
        """)
    @SerializedName(value = "advices", alternate = "entries")
    List<CommandAdviceEntry> advices = new ArrayList<>() {
        {
            this.add(new CommandAdviceEntry(true, new CommandAdviceEntry.Matcher("back", true), CommandAdviceType.BEFORE_EXECUTION, List.of("run as fake-op %player:name% say Before executing /back command for %player:name%")));
            this.add(new CommandAdviceEntry(true, new CommandAdviceEntry.Matcher("back", true), CommandAdviceType.AFTER_EXECUTION, List.of("run as fake-op %player:name% say After executing /back command for %player:name%")));

            this.add(new CommandAdviceEntry(true, new CommandAdviceEntry.Matcher("heal", true), CommandAdviceType.AFTER_EXECUTION, List.of(
                "say Display the heard particle for player %player:name%",
                "run as fake-op %player:name% --silent true particle minecraft:heart ~ ~1 ~ 0.6 0.6 0.6 0 20 force %player:name%")));

            this.add(new CommandAdviceEntry(true, new CommandAdviceEntry.Matcher("say (.+)", false), CommandAdviceType.CANCEL_WITH_SUCCESS, List.of(
                "send-broadcast <rb>[My Server]</rb> $1")));

            this.add(new CommandAdviceEntry(false, new CommandAdviceEntry.Matcher("(?:msg|tell) (\\S+?) (.+)", true), CommandAdviceType.CANCEL_WITH_SUCCESS, List.of(
                "send-message %player:name% <green>[PM] You -> $1: $2", "send-message $1 <green>[PM] %player:name% -> you: $2")));
        }
    };
}

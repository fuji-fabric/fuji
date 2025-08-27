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
            /* Decorate the `/heal` command with `heart particle`. */
            this.add(new CommandAdviceEntry(true, "Spawn a heart particle after the execution of `/heal` command.",new CommandAdviceEntry.Matcher("heal", true), CommandAdviceType.AFTER_EXECUTION, List.of(
                "say Display the heard particle for player %player:name%",
                "run as fake-op %player:name% --silent true particle minecraft:heart ~ ~1 ~ 0.6 0.6 0.6 0 20 force %player:name%")));

            /* Replace the `/say` with `/send-broadcast` implementation. */
            this.add(new CommandAdviceEntry(true, "Replace the execution of `/say` command with the `/send-broadcast` command.", new CommandAdviceEntry.Matcher("say (.+)", false), CommandAdviceType.CANCEL_AS_SUCCESS, List.of(
                "send-broadcast <rb>[My Server]</rb> $1")));

            /* Replace the `/msg` with `/send-message` implementation. */
            this.add(new CommandAdviceEntry(false, "Replace the execution of `/msg` command with our own DIY `/send-message` command.", new CommandAdviceEntry.Matcher("(?:msg|tell) (\\S+?) (.+)", true), CommandAdviceType.CANCEL_AS_SUCCESS, List.of(
                "send-message %player:name% <green>[PM] You -> $1: $2", "send-message $1 <green>[PM] %player:name% -> you: $2")));

            /* Guard command execution with specified requirements. */
            this.add(new CommandAdviceEntry(true, "Print a message before the execution of `/repair` command.", new CommandAdviceEntry.Matcher("repair", true), CommandAdviceType.BEFORE_EXECUTION, List.of("send-message %player:name% <pink>Before the execution of `/repair` command for %player:name%")));
            this.add(new CommandAdviceEntry(true, "Print a message after the execution of `/repair` command.", new CommandAdviceEntry.Matcher("repair", true), CommandAdviceType.AFTER_EXECUTION, List.of("send-message %player:name% <pink>After the execution of `/repair` command for %player:name%")));

            this.add(new CommandAdviceEntry(true, "Cancel the execution of `/repair` command, if the player doesn't have the required items in their inventory.", new CommandAdviceEntry.Matcher("repair", true), CommandAdviceType.CANCEL_IF_ANY_SUCCESS, List.of(
                "NOT has-item? %player:name% minecraft:iron_ingot 16",
                "NOT has-item? %player:name% minecraft:gold_ingot 16"
            )));

            this.add(new CommandAdviceEntry(true, "Send a feedback message when the execution of `/repair` command is cancelled.", new CommandAdviceEntry.Matcher("repair", true), CommandAdviceType.ON_EXECUTION_CANCELLED, List.of("send-message %player:name% <red>You need `iron_ingot x 16` and `gold_ingot x 16` to use the `/repair` command.")));

            this.add(new CommandAdviceEntry(true, "Take required items from the player's inventory, when the execution of `/repair` command is SUCCESS.", new CommandAdviceEntry.Matcher("repair", true), CommandAdviceType.ON_EXECUTION_SUCCESS, List.of(
                "send-message %player:name% The `/repair` command execution result is `SUCCESS`, I will take `iron_ingot x 16` and `gold_ingot x 16` from your inventory.",
                "clear %player:name% minecraft:iron_ingot 16",
                "clear %player:name% minecraft:gold_ingot 16"
            )));
            this.add(new CommandAdviceEntry(true, "Print a message, when the execution of `/repair` command is FAILURE.", new CommandAdviceEntry.Matcher("repair", true), CommandAdviceType.ON_EXECUTION_FAILURE, List.of(
                "send-message %player:name% The `/repair` command execution result is `FAILURE`, I will do nothing."
            )));

        }
    };
}

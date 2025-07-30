package io.github.sakurawald.fuji.module.initializer.command_advice.model;

import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.module.initializer.command_advice.structure.CommandAdviceEntry;
import io.github.sakurawald.fuji.module.initializer.command_advice.structure.CommandAdviceType;

import java.util.ArrayList;
import java.util.List;

public class CommandAdviceConfigModel {

    @Document(id = 1751826311892L, value = """
        Define `advices` for the `target command`.
        """)
    public final List<CommandAdviceEntry> entries = new ArrayList<>() {
        {
            this.add(new CommandAdviceEntry("back", true, CommandAdviceType.BEFORE_EXECUTING, List.of("run as fake-op %player:name% say before executing /back for %player:name%")));
            this.add(new CommandAdviceEntry("back", true, CommandAdviceType.AFTER_EXECUTING, List.of("run as fake-op %player:name% say after executing /back for %player:name%")));

            this.add(new CommandAdviceEntry("heal", true, CommandAdviceType.AFTER_EXECUTING, List.of(
                "say spawn a fireworks for player %player:name%",
                "run as fake-op %player:name% summon firework_rocket ~ ~1 ~ {FireworksItem:{id:firework_rocket,components:{fireworks:{explosions:[{shape:small_ball,colors:[I;15961002]}]}}}}")));

            this.add(new CommandAdviceEntry("heal", true, CommandAdviceType.AFTER_EXECUTING, List.of(
                "say spawn a fireworks for player %player:name%",
                "run as fake-op %player:name% summon firework_rocket ~ ~1 ~ {FireworksItem:{id:firework_rocket,components:{fireworks:{explosions:[{shape:small_ball,colors:[I;15961002]}]}}}}")));

            this.add(new CommandAdviceEntry("say (.+)", false, CommandAdviceType.CANCEL_WITH_SUCCESS, List.of(
                "send-broadcast <rb>[My Server]</rb> $1")));

            this.add(new CommandAdviceEntry("(?:msg|tell) (\\S+?) (.+)", true, CommandAdviceType.CANCEL_WITH_SUCCESS, List.of(
                "send-message %player:name% <green>[PM] You -> $1: $2", "send-message $1 <green>[PM] %player:name% -> you: $2")));
        }
    };
}

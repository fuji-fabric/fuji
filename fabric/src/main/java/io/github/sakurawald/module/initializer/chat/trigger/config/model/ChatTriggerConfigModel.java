package io.github.sakurawald.module.initializer.chat.trigger.config.model;

import io.github.sakurawald.module.initializer.chat.trigger.structure.ChatTrigger;

import java.util.ArrayList;
import java.util.List;

public class ChatTriggerConfigModel {

    public List<ChatTrigger> triggers = new ArrayList<>() {
        {
            this.add(new ChatTrigger("magic", List.of("say magic!")));
            this.add(new ChatTrigger("i am (.+)", List.of("say You just said: $0", "say Hello $1")));
            this.add(new ChatTrigger("(?<=^|\\s)item(?=\\s|$)", List.of("run as fake-op %player:name% chat display item")));
            this.add(new ChatTrigger("(?<=^|\\s)inv(?=\\s|$)", List.of("run as fake-op %player:name% chat display inv")));
            this.add(new ChatTrigger("(?<=^|\\s)ender(?=\\s|$)", List.of("run as fake-op %player:name% chat display ender")));
        }
    };
}

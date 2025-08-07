package io.github.sakurawald.fuji.module.initializer.chat.trigger.config.model;

import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.module.initializer.chat.trigger.structure.ChatTrigger;

import java.util.ArrayList;
import java.util.List;

public class ChatTriggerConfigModel {

    @Document(id = 1751826732919L, value = """
        Use `regex` expression to define `triggers`.
        """)
    public List<ChatTrigger> triggers = new ArrayList<>() {
        {
            this.add(ChatTrigger.make("magic", List.of("say magic!")));
            this.add(ChatTrigger.make("i am (.+)", List.of("say You just said: $0", "say Hello $1")));
            this.add(ChatTrigger.make("(?<=^|\\s)item(?=\\s|$)", List.of("run as fake-op %player:name% chat display item")));
            this.add(ChatTrigger.make("(?<=^|\\s)inv(?=\\s|$)", List.of("run as fake-op %player:name% chat display inv")));
            this.add(ChatTrigger.make("(?<=^|\\s)ender(?=\\s|$)", List.of("run as fake-op %player:name% chat display ender")));
        }
    };
}

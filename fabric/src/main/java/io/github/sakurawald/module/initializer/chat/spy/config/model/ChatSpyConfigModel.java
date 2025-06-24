package io.github.sakurawald.module.initializer.chat.spy.config.model;

import io.github.sakurawald.core.annotation.Document;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Data
@NoArgsConstructor
public class ChatSpyConfigModel {

    @Document("""
        Only accept and spy on `messages` whose `message type` meets the `whitelist`.
        """)
    public MessageType message_type = new MessageType();
    public static class MessageType {

        public List<String> whitelist = new ArrayList<>() {
            {
                this.add("minecraft:msg_command_incoming");
            }
        };

    }

    @Document("""
        Should not spy on `consecutive same text`.
        """)
    public boolean ignore_consecutive_same_text = true;

    @Document("""
        Should we also log the `console` what is spied?
        """)
    public boolean log_console = false;

    @Document("""
        Saved per-player options.
        """)
    public final HashMap<String, PerPlayerOptions> options = new HashMap<>();

    @Data
    @NoArgsConstructor
    public static class PerPlayerOptions {
        @Document("""
            Is `chat spy` mode enabled?
            """)
        public boolean enabled = false;
    }

}

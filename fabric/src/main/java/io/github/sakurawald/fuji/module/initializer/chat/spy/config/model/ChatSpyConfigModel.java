package io.github.sakurawald.fuji.module.initializer.chat.spy.config.model;

import io.github.sakurawald.fuji.core.document.annotation.Document;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Data
@NoArgsConstructor
public class ChatSpyConfigModel {

    @Document(id = 1751826718752L, value = """
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

    @Document(id = 1751826720527L, value = """
        Should not spy on `consecutive same text`.
        """)
    public boolean ignore_consecutive_same_text = true;

    @Document(id = 1751826722259L, value = """
        Should we also log the `console` what is spied?
        """)
    public boolean log_console = false;

    @Document(id = 1751826724306L, value = """
        Saved per-player options.
        """)
    public final HashMap<String, PerPlayerOptions> options = new HashMap<>();

    @Data
    @NoArgsConstructor
    public static class PerPlayerOptions {
        @Document(id = 1751826726521L, value = """
            Is `chat spy` mode enabled?
            """)
        public boolean enabled = false;
    }

}

package io.github.sakurawald.module.initializer.chat.spy.config.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Data
@NoArgsConstructor
public class ChatSpyConfigModel {

    public MessageType message_type = new MessageType();

    public static class MessageType {

        public List<String> whitelist = new ArrayList<>() {
            {
                this.add("minecraft:msg_command_incoming");
            }
        };

    }

    public boolean ignore_consecutive_same_text = true;

    public boolean log_console = false;

    public final HashMap<String, PerPlayerOptions> options = new HashMap<>();

    @Data
    @NoArgsConstructor
    public static class PerPlayerOptions {
        public boolean enabled = false;
    }

}

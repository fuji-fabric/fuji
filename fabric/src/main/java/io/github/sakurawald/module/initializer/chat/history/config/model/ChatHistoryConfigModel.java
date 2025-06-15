package io.github.sakurawald.module.initializer.chat.history.config.model;

import java.util.ArrayList;
import java.util.List;

public class ChatHistoryConfigModel {
    public int buffer_size = 50;

    public List<String> message_type_filters = new ArrayList<>() {
        {
            this.add("minecraft:chat");
            this.add("minecraft:say_command");
            this.add("minecraft:emote_command");
            this.add("fuji:chat_server");
            this.add("fuji:chat_client");
            this.add("styled_chat:generic_hack");
        }
    };

    public MessageRejectors message_rejectors = new MessageRejectors();
    public static class MessageRejectors {

        public ContentRejector content_rejector = new ContentRejector();
        public static class ContentRejector {
            public List<String> rules = new ArrayList<>() {};
        }

        public ParameterRejector parameter_rejector = new ParameterRejector();
        public static class ParameterRejector {
            public List<String> rules = new ArrayList<>() {
                {
                    this.add("literal{PM}");
                }
            };
        }
    }
}

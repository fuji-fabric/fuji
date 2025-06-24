package io.github.sakurawald.module.initializer.chat.history.config.model;

import io.github.sakurawald.core.annotation.Document;

import java.util.ArrayList;
import java.util.List;

public class ChatHistoryConfigModel {
    @Document("""
        Max stored `chat message` in history.
        """)
    public int buffer_size = 50;

    @Document("""
        Only accept and save messages with these `message types`.
        """)
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

    @Document("""
        Should reject and never save messages that meet the `rejector`.
        """)
    public MessageRejectors message_rejectors = new MessageRejectors();
    public static class MessageRejectors {

        @Document("""
            Should reject and never save messages whose `content` meets the rejector.
            """)
        public ContentRejector content_rejector = new ContentRejector();
        public static class ContentRejector {
            @Document("""
                Define `regex` expression to match `message content`
                """)
            public List<String> rules = new ArrayList<>() {};
        }

        @Document("""
            Should reject and never save messages whose `parameter` meets the rejector.
            """)
        public ParameterRejector parameter_rejector = new ParameterRejector();
        public static class ParameterRejector {
            @Document("""
                Use `regex` expression to match `message parameter`.

                Issue `/fuji debug` to see the `parameter` of a message.
                """)

            public List<String> rules = new ArrayList<>() {
                {
                    this.add("literal{PM}");
                }
            };
        }
    }
}

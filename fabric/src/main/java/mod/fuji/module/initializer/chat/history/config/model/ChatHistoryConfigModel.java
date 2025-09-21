package mod.fuji.module.initializer.chat.history.config.model;

import com.google.gson.annotations.SerializedName;
import mod.fuji.core.document.annotation.Document;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ChatHistoryConfigModel {

    @Document(id = 1751826688467L, value = """
        Max stored `chat message` in history.
        """)
    int bufferSize = 50;

    @Document(id = 1751826690768L, value = """
        Only accept and save messages with these `message types`.
        """)
    @SerializedName(value = "message_type_acceptors", alternate = "message_type_filters")
    List<String> messageTypeAcceptors = new ArrayList<>() {
        {
            this.add("minecraft:chat");
            this.add("minecraft:say_command");
            this.add("minecraft:emote_command");
            this.add("fuji:chat_server");
            this.add("fuji:chat_client");
            this.add("styled_chat:generic_hack");
        }
    };

    @Document(id = 1751826693489L, value = """
        Should reject and never save messages that meet the `rejector`.
        """)
    MessageRejectors messageRejectors = new MessageRejectors();

    @Data
    @NoArgsConstructor
    public static class MessageRejectors {

        @Document(id = 1751826695706L, value = """
            Should reject and never save messages whose `content` meets the rejector.
            """)
        ContentRejector contentRejector = new ContentRejector();

        @Data
        @NoArgsConstructor
        public static class ContentRejector {
            @Document(id = 1751826699229L, value = """
                Define `regex` expression to match `message content`
                """)
            List<String> matches = new ArrayList<>() {};
        }

        @Document(id = 1751826702393L, value = """
            Should reject and never save messages whose `parameter` meets the rejector.
            """)
        ParameterRejector parameterRejector = new ParameterRejector();

        @Data
        @NoArgsConstructor
        public static class ParameterRejector {
            @Document(id = 1751826704630L, value = """
                Use `regex` expression to match `message parameter`.

                Issue `/fuji debug` to see the `parameter` of a message.
                """)
            List<String> matches = new ArrayList<>() {
                {
                    this.add(".*literal\\{PM\\}.*");
                }
            };
        }
    }
}

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
        Maximum number of chat texts stored in chat history.
        """)
    @SerializedName(value = "max_chat_history_size", alternate = "buffer_size")
    int maxChatHistorySize = 50;

    @Document(id = 1751826690768L, value = """
        A `chat text` sent by a player will be `stored`, if its `message type` is one of the defined types.
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
        A `chat text` sent by a player will be `ignored`, if it satisfy one of the defined rejectors.
        """)
    MessageRejectors messageRejectors = new MessageRejectors();

    @Data
    @NoArgsConstructor
    public static class MessageRejectors {

        @Document(id = 1751826695706L, value = """
            A `chat text` sent by a player will be `ignored`, if its `content` matches one of the defined rejector.
            """)
        ContentRejector contentRejector = new ContentRejector();

        @Data
        @NoArgsConstructor
        public static class ContentRejector {
            List<String> matches = new ArrayList<>() {};
        }

        @Document(id = 1751826702393L, value = """
            A `chat text` sent by a player will be `ignored`, if its `parameter` matches one of the defined rejector.
            """)
        ParameterRejector parameterRejector = new ParameterRejector();

        @Data
        @NoArgsConstructor
        public static class ParameterRejector {
            List<String> matches = new ArrayList<>() {
                {
                    this.add(".*literal\\{PM\\}.*");
                }
            };
        }
    }
}

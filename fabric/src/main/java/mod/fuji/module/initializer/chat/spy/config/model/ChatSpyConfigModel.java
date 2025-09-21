package mod.fuji.module.initializer.chat.spy.config.model;

import com.google.gson.annotations.SerializedName;
import mod.fuji.core.document.annotation.Document;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class ChatSpyConfigModel {

    @Document(id = 1751826718752L, value = """
        Only accept and spy on `messages` whose `message type` meets the `whitelist`.
        """)
    MessageType messageType = new MessageType();

    @Data
    @NoArgsConstructor
    public static class MessageType {

        @SerializedName(value = "acceptors", alternate = "whitelist")
        List<String> acceptors = new ArrayList<>() {
            {
                this.add("minecraft:msg_command_incoming");
            }
        };

    }

    @Document(id = 1751826720527L, value = """
        Should not spy on `consecutive same text`.
        """)
    boolean ignoreConsecutiveSameText = true;

    @Document(id = 1751826722259L, value = """
        Should we also log the `console` what is spied?
        """)
    boolean logConsole = false;

}

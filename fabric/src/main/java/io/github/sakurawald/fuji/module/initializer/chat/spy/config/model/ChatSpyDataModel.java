package io.github.sakurawald.fuji.module.initializer.chat.spy.config.model;

import io.github.sakurawald.fuji.core.document.annotation.Document;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ChatSpyDataModel {

    @Document(id = 1751826724306L, value = """
        Saved per-player options.
        """)
    Map<String, PerPlayerOptions> options = new HashMap<>();

    @Data
    @NoArgsConstructor
    public static class PerPlayerOptions {
        @Document(id = 1751826726521L, value = """
            Is `chat spy` mode enabled?
            """)
        boolean enabled = false;
    }

}

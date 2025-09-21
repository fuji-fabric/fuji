package mod.fuji.module.initializer.chat.style.model;

import mod.fuji.core.document.annotation.Document;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ChatFormatModel {

    @Document(id = 1751826664764L, value = """
        Per-player chat content format.
        """)
    Format format = new Format();

    @Data
    @NoArgsConstructor
    public static class Format {
        Map<String, String> player2format = new HashMap<>() {
            {
                this.put("Steve", "<#FFC7EA>%message%");
            }
        };
    }
}

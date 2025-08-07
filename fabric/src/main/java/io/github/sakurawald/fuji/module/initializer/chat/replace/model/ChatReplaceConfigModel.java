package io.github.sakurawald.fuji.module.initializer.chat.replace.model;

import com.google.gson.annotations.SerializedName;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.structure.RegexRewriteNode;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ChatReplaceConfigModel {

    @Document(id = 1751826659782L, value = """
        Define `regex` expression, to replace `chat string`.
        """)
    Replace replace = new Replace();

    @Data
    @NoArgsConstructor
    public static class Replace {
        @SerializedName(value = "rules", alternate = "regex")
        List<RegexRewriteNode> rules = new ArrayList<>() {
            {
                this.add(new RegexRewriteNode("(?<=^|\\s)item(?=\\s|$)", "%fuji:item%"));
                this.add(new RegexRewriteNode("(?<=^|\\s)inv(?=\\s|$)", "%fuji:inv%"));
                this.add(new RegexRewriteNode("(?<=^|\\s)ender(?=\\s|$)", "%fuji:ender%"));

                this.add(new RegexRewriteNode("(?<=^|\\s)pos(?=\\s|$)", "%fuji:pos%"));
                this.add(new RegexRewriteNode("(?<=^|\\s)uuid(?=\\s|$)", "<green>my uuid is %player:uuid%</green>"));
            }
        };
    }
}

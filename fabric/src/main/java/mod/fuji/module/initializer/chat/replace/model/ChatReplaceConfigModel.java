package mod.fuji.module.initializer.chat.replace.model;

import com.google.gson.annotations.SerializedName;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.structure.RegexRewriteRule;

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
        List<RegexRewriteRule> rules = new ArrayList<>() {
            {
                this.add(new RegexRewriteRule("(?<=^|\\s)item(?=\\s|$)", "%fuji:item%"));
                this.add(new RegexRewriteRule("(?<=^|\\s)inv(?=\\s|$)", "%fuji:inv%"));
                this.add(new RegexRewriteRule("(?<=^|\\s)ender(?=\\s|$)", "%fuji:ender%"));

                this.add(new RegexRewriteRule("(?<=^|\\s)pos(?=\\s|$)", "%fuji:pos%"));
                this.add(new RegexRewriteRule("(?<=^|\\s)uuid(?=\\s|$)", "<green>My uuid is %player:uuid%</green>"));
            }
        };
    }
}

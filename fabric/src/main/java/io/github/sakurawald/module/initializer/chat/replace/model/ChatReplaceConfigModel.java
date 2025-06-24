package io.github.sakurawald.module.initializer.chat.replace.model;

import io.github.sakurawald.core.annotation.Document;
import io.github.sakurawald.core.structure.RegexRewriteNode;

import java.util.ArrayList;
import java.util.List;

public class ChatReplaceConfigModel {

    @Document("""
        Define `regex` expression, to replace `chat string`.
        """)
    public Replace replace = new Replace();
    public static class Replace {
        public List<RegexRewriteNode> regex = new ArrayList<>() {
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

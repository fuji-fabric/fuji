package io.github.sakurawald.module.initializer.chat.rewrite.config.model;

import io.github.sakurawald.core.structure.RegexRewriteNode;

import java.util.ArrayList;
import java.util.List;

public class ChatRewriteConfigModel {

    public Rewrite rewrite = new Rewrite();

    public static class Rewrite {
        public List<RegexRewriteNode> regex = new ArrayList<>() {
            {
                this.add(new RegexRewriteNode("(?<=^|\\s)item(?=\\s|$)", "[item]"));
                this.add(new RegexRewriteNode("(?<=^|\\s)inv(?=\\s|$)", "[inv]"));
                this.add(new RegexRewriteNode("(?<=^|\\s)ender(?=\\s|$)", "[ender]"));
                this.add(new RegexRewriteNode("((https?)://[^\\s/$.?#].\\S*)", "<underline><blue><hover:show_text:'$1'><click:open_url:'$1'>$1</click></hover></blue></underline>"));
                this.add(new RegexRewriteNode("^BV(\\w{10})", "<underline><blue><hover:show_text:'$1'><click:open_url:'https://www.bilibili.com/video/BV$1'>bilibili $1</click></hover></blue></underline>"));
            }
        };
    }
}

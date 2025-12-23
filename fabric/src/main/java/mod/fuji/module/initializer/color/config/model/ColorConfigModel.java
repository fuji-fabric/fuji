package mod.fuji.module.initializer.color.config.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import mod.fuji.core.structure.RegexRewriteNode;

@Data
@NoArgsConstructor
public class ColorConfigModel {

    Rewrite rewrite = new Rewrite();

    @Data
    @NoArgsConstructor
    public static class Rewrite {
        List<RegexRewriteNode> rules = new ArrayList<>() {
            {
                this.add(new RegexRewriteNode("&0", "<black>"));
                this.add(new RegexRewriteNode("&1", "<dark_blue>"));
                this.add(new RegexRewriteNode("&2", "<dark_green>"));
                this.add(new RegexRewriteNode("&3", "<dark_aqua>"));
                this.add(new RegexRewriteNode("&4", "<dark_red>"));
                this.add(new RegexRewriteNode("&5", "<dark_purple>"));
                this.add(new RegexRewriteNode("&6", "<gold>"));
                this.add(new RegexRewriteNode("&7", "<gray>"));
                this.add(new RegexRewriteNode("&8", "<dark_gray>"));
                this.add(new RegexRewriteNode("&9", "<blue>"));

                this.add(new RegexRewriteNode("&a", "<green>"));
                this.add(new RegexRewriteNode("&b", "<aqua>"));
                this.add(new RegexRewriteNode("&c", "<red>"));
                this.add(new RegexRewriteNode("&d", "<light_purple>"));
                this.add(new RegexRewriteNode("&e", "<yellow>"));
                this.add(new RegexRewriteNode("&f", "<white>"));

                this.add(new RegexRewriteNode("&k", "<obf>"));
                this.add(new RegexRewriteNode("&l", "<b>"));
                this.add(new RegexRewriteNode("&m", "<st>"));
                this.add(new RegexRewriteNode("&n", "<u>"));
                this.add(new RegexRewriteNode("&o", "<i>"));
                this.add(new RegexRewriteNode("&r", "<r>"));
            }

        };

    }

}

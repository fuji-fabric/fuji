package mod.fuji.module.initializer.color.config.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import mod.fuji.core.structure.RegexRewriteRule;

@Data
@NoArgsConstructor
public class ColorConfigModel {

    Rewrite rewrite = new Rewrite();

    @Data
    @NoArgsConstructor
    public static class Rewrite {
        List<RegexRewriteRule> rules = new ArrayList<>() {
            {
                this.add(new RegexRewriteRule("&0", "<black>"));
                this.add(new RegexRewriteRule("&1", "<dark_blue>"));
                this.add(new RegexRewriteRule("&2", "<dark_green>"));
                this.add(new RegexRewriteRule("&3", "<dark_aqua>"));
                this.add(new RegexRewriteRule("&4", "<dark_red>"));
                this.add(new RegexRewriteRule("&5", "<dark_purple>"));
                this.add(new RegexRewriteRule("&6", "<gold>"));
                this.add(new RegexRewriteRule("&7", "<gray>"));
                this.add(new RegexRewriteRule("&8", "<dark_gray>"));
                this.add(new RegexRewriteRule("&9", "<blue>"));

                this.add(new RegexRewriteRule("&a", "<green>"));
                this.add(new RegexRewriteRule("&b", "<aqua>"));
                this.add(new RegexRewriteRule("&c", "<red>"));
                this.add(new RegexRewriteRule("&d", "<light_purple>"));
                this.add(new RegexRewriteRule("&e", "<yellow>"));
                this.add(new RegexRewriteRule("&f", "<white>"));

                this.add(new RegexRewriteRule("&k", "<obf>"));
                this.add(new RegexRewriteRule("&l", "<b>"));
                this.add(new RegexRewriteRule("&m", "<st>"));
                this.add(new RegexRewriteRule("&n", "<u>"));
                this.add(new RegexRewriteRule("&o", "<i>"));
                this.add(new RegexRewriteRule("&r", "<r>"));
            }

        };

    }

}

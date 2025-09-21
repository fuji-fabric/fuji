package mod.fuji.core.structure;

import mod.fuji.core.document.annotation.Document;
import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
public class RegexRewriteNode {
    @Document(id = 1751823950782L, value = """
        The `regex` expression used to match the `target string`.
        """)
    String regex;

    @Document(id = 1751823954950L, value = """
        The `pattern` used to replace the `matched target string`.
        """)
    String replacement;

    @ToString.Exclude
    @Getter(AccessLevel.NONE)
    transient Pattern pattern;

    public RegexRewriteNode(String regex, String replacement) {
        this.regex = regex;
        this.replacement = replacement;
    }

    public Pattern getCachedPattern() {
        if (this.pattern == null) {
            this.pattern = Pattern.compile(this.regex);
        }

        return this.pattern;
    }

}

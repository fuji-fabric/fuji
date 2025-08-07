package io.github.sakurawald.fuji.core.structure;

import io.github.sakurawald.fuji.core.document.annotation.Document;
import java.util.regex.Pattern;
import lombok.Data;
import lombok.ToString;

@Data
public class RegexRewriteNode {
    @Document(id = 1751823950782L, value = """
        The `regex` expression used to match the `target string`.
        """)
    final String regex;

    @Document(id = 1751823954950L, value = """
        The `pattern` used to replace the `matched target string`.
        """)
    final String replacement;

    @ToString.Exclude
    transient Pattern pattern;

    public Pattern getCachedPattern() {
        if (pattern == null) {
            pattern = Pattern.compile(regex);
        }

        return pattern;
    }

}

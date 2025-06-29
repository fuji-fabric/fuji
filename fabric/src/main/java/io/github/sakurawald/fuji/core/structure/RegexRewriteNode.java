package io.github.sakurawald.fuji.core.structure;

import io.github.sakurawald.fuji.core.document.annotation.Document;
import lombok.Data;

@Data
public class RegexRewriteNode {
    @Document("""
        The `regex` expression used to match the `target string`.
        """)
    final String regex;

    @Document("""
        The `pattern` used to replace the `matched target string`.
        """)
    final String replacement;
}

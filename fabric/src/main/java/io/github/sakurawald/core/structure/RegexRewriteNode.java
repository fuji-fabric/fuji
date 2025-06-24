package io.github.sakurawald.core.structure;

import io.github.sakurawald.core.annotation.Document;
import lombok.Data;

@SuppressWarnings("unused")
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

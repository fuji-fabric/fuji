package io.github.sakurawald.core.structure;

import lombok.Data;

@SuppressWarnings("unused")
@Data
public class RegexRewriteNode {
    final String regex;
    final String replacement;
}

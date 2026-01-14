package mod.fuji.core.structure;

import java.util.Optional;
import java.util.regex.Matcher;
import lombok.AccessLevel;
import lombok.Setter;
import mod.fuji.core.auxiliary.LogUtil;
import mod.fuji.core.document.annotation.Document;
import java.util.regex.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

@Data
@NoArgsConstructor
public class RegexRewriteRule {

    @Document(id = 1751823950782L, value = """
        The `pattern string` used to match the `target string`.
        """)
    String regex;

    @Document(id = 1751823954950L, value = """
        The `replacement string` used to replace the `matched target string`.
        """)
    String replacement;

    @ToString.Exclude
    @Setter(value = AccessLevel.NONE)
    transient Optional<Pattern> pattern = Optional.empty();

    public RegexRewriteRule(@NotNull String regex, @NotNull String replacement) {
        this.regex = regex;
        this.replacement = replacement;
    }

    public @NotNull String apply(@NotNull String input) {
        /* Ignore malformed regex rewrite rule. */
        Optional<Pattern> pattern = this.getPattern();
        if (pattern.isEmpty()) {
            LogUtil.warn("Failed to apply the regex rewrite rule: {} (This rule is malformed.)", this);
            return input;
        }

        /* Rewrite the input string using the rule. */
        Pattern $pattern = pattern.get();
        Matcher matcher = $pattern.matcher(input);
        input = matcher.replaceAll(this.getReplacement());
        return input;
    }

    public @NotNull Optional<Pattern> getPattern() {
        /* Compile the pattern if empty. */
        if (this.pattern.isEmpty()) {
            try {
                Pattern pattern = Pattern.compile(this.regex);
                this.pattern = Optional.of(pattern);
            } catch (Exception e) {
                LogUtil.error("Failed to compile the regex string '{}'. (Regex Syntax Error)", this.regex, e);
            }
        }

        /* Return the cached pattern. */
        return this.pattern;
    }

}

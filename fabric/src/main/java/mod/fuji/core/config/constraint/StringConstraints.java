package mod.fuji.core.config.constraint;

import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.command.exception.AbortCommandExecutionException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StringConstraints {

    int maxLength;

    @Nullable String acceptStringPattern;

    @Nullable String rejectStringPattern;

    public @Nullable String apply(@Nullable String value, @NotNull Object audience) {
        if (value == null) return null;

        /* Apply the constraint for length. */
        if (value.length() > maxLength) {
            TextHelper.sendTextByKey(audience, "constraint.length.overflow", TextHelper.Parsers.escapeTags(value));
            throw new AbortCommandExecutionException();
        }

        /* Apply the constraint for pattern. */
        Optional
            .ofNullable(acceptStringPattern)
            .ifPresent($acceptStringPattern -> {
                if (!value.matches($acceptStringPattern)) {
                    TextHelper.sendTextByKey(audience, "constraint.pattern.matcher.accept.failed", TextHelper.Parsers.escapeTags(value));
                    throw new AbortCommandExecutionException();
                }
            });

        Optional
            .ofNullable(rejectStringPattern)
            .ifPresent($rejectStringPattern -> {
                if (value.matches($rejectStringPattern)) {
                    TextHelper.sendTextByKey(audience, "constraint.pattern.matcher.reject.failed", TextHelper.Parsers.escapeTags(value));
                    throw new AbortCommandExecutionException();
                }
            });


        return value;
    }

}

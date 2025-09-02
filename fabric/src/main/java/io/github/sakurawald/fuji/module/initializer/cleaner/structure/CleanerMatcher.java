package io.github.sakurawald.fuji.module.initializer.cleaner.structure;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CleanerMatcher {

    boolean enable;

    String translatableKey;

    int livesLongerThanAge;

    CleanupMethod cleanupMethod;

    public boolean isMatch(@NotNull String entityTranslatableKey, int entityAge) {
        return this.translatableKey.equals(entityTranslatableKey)
            && entityAge > livesLongerThanAge;
    }

}

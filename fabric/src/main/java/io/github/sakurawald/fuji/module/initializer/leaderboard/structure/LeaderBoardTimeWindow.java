package io.github.sakurawald.fuji.module.initializer.leaderboard.structure;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.config.annotation.NotNullEnumType;
import org.jetbrains.annotations.Nullable;

@NotNullEnumType
public enum LeaderBoardTimeWindow {
    HOURLY("hourly"), DAILY("daily"), WEEKLY("weekly"), MONTHLY("monthly"), YEARLY("yearly"), ALL_TIME("all_time");

    final String languageKey;

    LeaderBoardTimeWindow(String languageKey) {
        this.languageKey = languageKey;
    }

    public String toLanguageValue(@Nullable Object audience) {
        return TextHelper.Translator.getLanguageValueByKey(audience, this.languageKey);
    }
}

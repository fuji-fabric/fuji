package io.github.sakurawald.fuji.module.initializer.command_advice.structure;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
public enum CommandAdviceType {
    BEFORE_EXECUTING(CommandHelper.Return.SUCCESS),
    AFTER_EXECUTING(CommandHelper.Return.SUCCESS),
    CANCEL_WITH_SUCCESS(CommandHelper.Return.SUCCESS),
    CANCEL_WITH_FAILURE(CommandHelper.Return.FAILURE);

    final int alternativeReturnValue;

    CommandAdviceType(int alternativeReturnValue) {
        this.alternativeReturnValue = alternativeReturnValue;
    }

    public static boolean isCancellableAdviceType(@NotNull CommandAdviceType commandAdviceType) {
        return commandAdviceType.equals(CANCEL_WITH_SUCCESS) || commandAdviceType.equals(CANCEL_WITH_FAILURE);
    }
}

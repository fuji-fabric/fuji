package io.github.sakurawald.fuji.module.initializer.command_advice.structure;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import lombok.Getter;

@Getter
public enum CommandAdviceType {
    BEFORE_EXECUTING(false, CommandHelper.Return.SUCCESS),
    AFTER_EXECUTING(false, CommandHelper.Return.SUCCESS),
    CANCEL_WITH_SUCCESS(true, CommandHelper.Return.SUCCESS),
    CANCEL_WITH_FAILURE(true, CommandHelper.Return.FAILURE),
    CANCEL_IF_ANY_SUCCESS(true, CommandHelper.Return.FAILURE),
    CANCEL_IF_ALL_SUCCESS(true, CommandHelper.Return.FAILURE);

    final boolean isCancellable;
    final int alternativeReturnValue;

    CommandAdviceType(boolean isCancellable, int alternativeReturnValue) {
        this.alternativeReturnValue = alternativeReturnValue;
        this.isCancellable = isCancellable;
    }

}

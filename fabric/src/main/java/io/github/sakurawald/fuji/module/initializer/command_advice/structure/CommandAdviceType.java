package io.github.sakurawald.fuji.module.initializer.command_advice.structure;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.document.annotation.ForDeveloper;
import lombok.Getter;

@Getter
public enum CommandAdviceType {
    BEFORE_EXECUTING(false, CommandHelper.Return.SUCCESS),
    AFTER_EXECUTING(false, CommandHelper.Return.SUCCESS),
    ON_EXECUTION_SUCCESS(false, CommandHelper.Return.SUCCESS),
    ON_EXECUTION_FAILURE(false, CommandHelper.Return.FAILURE),
    ON_EXECUTION_CANCELLED(false, CommandHelper.Return.SUCCESS),
    CANCEL_WITH_SUCCESS(true, CommandHelper.Return.SUCCESS),
    CANCEL_WITH_FAILURE(true, CommandHelper.Return.FAILURE),
    CANCEL_IF_ANY_SUCCESS(true, CommandHelper.Return.FAILURE),
    CANCEL_IF_ALL_SUCCESS(true, CommandHelper.Return.FAILURE);

    @ForDeveloper("A canceller advice will be accepted by BEFORE_EXECUTING type.")
    final boolean isCanceller;
    final int alternativeReturnValue;

    CommandAdviceType(boolean isCanceller, int alternativeReturnValue) {
        this.isCanceller = isCanceller;
        this.alternativeReturnValue = alternativeReturnValue;
    }
}

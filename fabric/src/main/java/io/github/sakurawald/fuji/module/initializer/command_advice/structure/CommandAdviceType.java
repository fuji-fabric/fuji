package io.github.sakurawald.fuji.module.initializer.command_advice.structure;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.document.annotation.ForDeveloper;
import lombok.Getter;

@Getter
public enum CommandAdviceType {
    BEFORE_EXECUTING(false, false, CommandHelper.Return.SUCCESS),
    AFTER_EXECUTING(false, false, CommandHelper.Return.SUCCESS),
    CANCEL_WITH_SUCCESS(true, false, CommandHelper.Return.SUCCESS),
    CANCEL_WITH_FAILURE(true, false, CommandHelper.Return.FAILURE),
    CANCEL_IF_ANY_SUCCESS(true, false, CommandHelper.Return.FAILURE),
    CANCEL_IF_ALL_SUCCESS(true, false, CommandHelper.Return.FAILURE),
    ON_CANCELLED(false, true, CommandHelper.Return.SUCCESS),;

    @ForDeveloper("A canceller advice will be accepted by BEFORE_EXECUTING type.")
    final boolean isCanceller;
    @ForDeveloper("A monitor advice will be accepted by any type.")
    final boolean isMonitor;
    final int alternativeReturnValue;

    CommandAdviceType(boolean isCanceller, boolean isMonitor, int alternativeReturnValue) {
        this.isCanceller = isCanceller;
        this.isMonitor = isMonitor;
        this.alternativeReturnValue = alternativeReturnValue;
    }
}

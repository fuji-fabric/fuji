package io.github.sakurawald.fuji.module.initializer.command_advice.structure;

import com.google.gson.annotations.SerializedName;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import lombok.Getter;

@Getter
public enum CommandAdviceType {
    @SerializedName(value = "BEFORE_EXECUTION", alternate = "BEFORE_EXECUTING")
    BEFORE_EXECUTION(false, CommandHelper.Return.SUCCESS),

    @SerializedName(value = "AFTER_EXECUTION", alternate = "AFTER_EXECUTING")
    AFTER_EXECUTION(false, CommandHelper.Return.SUCCESS),

    ON_EXECUTION_SUCCESS(false, CommandHelper.Return.SUCCESS),

    ON_EXECUTION_FAILURE(false, CommandHelper.Return.FAILURE),

    ON_EXECUTION_CANCELLED(false, CommandHelper.Return.SUCCESS),

    @SerializedName(value = "CANCEL_AS_SUCCESS", alternate = "CANCEL_WITH_SUCCESS")
    CANCEL_AS_SUCCESS(true, CommandHelper.Return.SUCCESS),

    @SerializedName(value = "CANCEL_AS_FAILURE", alternate = "CANCEL_WITH_FAILURE")
    CANCEL_AS_FAILURE(true, CommandHelper.Return.FAILURE),

    CANCEL_IF_ANY_SUCCESS(true, CommandHelper.Return.FAILURE),

    CANCEL_IF_ALL_SUCCESS(true, CommandHelper.Return.FAILURE);

    final boolean isCanceller;
    final int alternativeReturnValue;

    CommandAdviceType(boolean isCanceller, int alternativeReturnValue) {
        this.isCanceller = isCanceller;
        this.alternativeReturnValue = alternativeReturnValue;
    }
}

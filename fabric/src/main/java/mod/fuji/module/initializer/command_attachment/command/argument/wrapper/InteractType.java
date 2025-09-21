package mod.fuji.module.initializer.command_attachment.command.argument.wrapper;

import com.google.gson.annotations.SerializedName;
import mod.fuji.core.config.annotation.NotNullEnumType;

@NotNullEnumType
public enum InteractType {
    @SerializedName(value = "LEFT_CLICK", alternate = "LEFT")
    LEFT_CLICK,
    @SerializedName(value = "RIGHT_CLICK", alternate = "RIGHT")
    RIGHT_CLICK,
    @SerializedName(value = "ANY_CLICK", alternate = "BOTH")
    ANY_CLICK,

    SWAP_HAND,

    STEP_ON
}

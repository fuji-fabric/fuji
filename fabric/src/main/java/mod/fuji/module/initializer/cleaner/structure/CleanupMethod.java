package mod.fuji.module.initializer.cleaner.structure;

import com.google.gson.annotations.SerializedName;
import mod.fuji.core.config.annotation.NotNullEnumType;

@NotNullEnumType
public enum CleanupMethod {

    KILL,

    @SerializedName(value = "DELETE", alternate = "DISCARD")
    DELETE

}

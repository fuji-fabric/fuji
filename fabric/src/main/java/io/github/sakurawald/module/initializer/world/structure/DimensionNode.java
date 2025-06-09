package io.github.sakurawald.module.initializer.world.structure;

import com.google.gson.annotations.SerializedName;
import io.github.sakurawald.core.auxiliary.minecraft.RegistryHelper;
import io.github.sakurawald.core.auxiliary.minecraft.ServerHelper;
import lombok.AllArgsConstructor;
import lombok.Data;

@SuppressWarnings("unused")
@Data
@AllArgsConstructor
public class DimensionNode {
    boolean enable;
    String dimension;
    @SerializedName(value = "dimension_type", alternate = "dimensionType")
    String dimension_type;
    long seed;

    public boolean isDimensionLoaded() {
        return ServerHelper.getWorlds().stream().anyMatch(it -> RegistryHelper.ofString(it).equals(this.dimension));
    }
}

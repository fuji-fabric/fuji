package io.github.sakurawald.fuji.module.initializer.world.structure;

import com.google.gson.annotations.SerializedName;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.RegistryHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.ServerHelper;
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

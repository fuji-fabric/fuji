package io.github.sakurawald.fuji.module.initializer.world.structure;

import com.google.gson.annotations.SerializedName;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.RegistryHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import net.minecraft.world.Difficulty;

@Document(id = 1752170874671L, value = """
    A `dimension node` is used to describe a created `extra dimension`.
    It contains the `meta data` of a `dimension`.
    """)
@Data
public class DimensionNode {
    @Document(id = 1752170969085L, value = """
        Should we `load` this `dimension` on server startup?
        """)
    public boolean enable = true;

    @Document(id = 1752170986625L, value = """
        The `identifier` of this `dimension`.
        """)
    public String dimension;

    @Document(id = 1752171006784L, value = """
        The `dimension type` of this `dimension`.
        Note that the `dimension type` defines the `chunk generator` and `dimension features`.
        """)
    @SerializedName(value = "dimension_type", alternate = "dimensionType")
    public String dimension_type;

    @Document(id = 1752246679197L, value = """
        The `seed` used for the chunk generator of this dimension.
        """)
    public long seed;

    @Document(id = 1752246657296L, value = """
        Should we tick the time of this dimension? (Do the day night cycle?)
        """)
    public boolean shouldTickTime = true;

    public Difficulty difficulty = Difficulty.NORMAL;
    public long timeOfDay = 6000;

    public boolean isDimensionLoaded() {
        return ServerHelper
            .getWorlds()
            .stream()
            .anyMatch(it -> RegistryHelper.toString(it).equals(this.dimension));
    }
}

package io.github.sakurawald.fuji.module.initializer.world.manager.structure;

import com.google.gson.annotations.SerializedName;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.RegistryHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.module.initializer.world.manager.command.argument.wrapper.ChunkGeneratorType;
import io.github.sakurawald.fuji.module.initializer.world.manager.command.argument.wrapper.WorldPresetType;
import java.util.Optional;
import lombok.Data;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.Difficulty;
import org.jetbrains.annotations.Nullable;

@Document(id = 1752170874671L, value = """
    A `dimension descriptor` is used to describe a `runtime dimension`.
    It contains the important info about a `dimension`, like the `dimension id`, `dimension type` and `seed`...
    """)
@Data
public class RuntimeDimensionDescriptor {

    @Document(id = 1752170969085L, value = """
        Should we `load` this `dimension` on server startup?
        """)
    @SerializedName(value = "auto_load_on_server_startup", alternate = "enable")
    public boolean auto_load_on_server_startup = true;

    @Document(id = 1752170986625L, value = """
        The `identifier` of this `dimension`.
        """)
    public String dimension = null;

    @Document(id = 1752738994765L, value = """
        The `world preset type` used by this `dimension`.

        If `world preset type` is specified, then `dimension type`, `chunk generator type` and `chunk generator parameters` are ignored.
        """)
    @Nullable public WorldPresetType worldPresetType = null;

    @Document(id = 1752171006784L, value = """
        The `dimension type` of this `dimension`.

        For `FlatChunkGenerator`:
        1. The `dimension type` defines the `dimension environment`.

        For `NoiseChunkGenerator`:
        1. The `dimension type` defines the `dimension environment`.
        2. The `dimension type` defines the `chunk generator`.

        <green>NOTE: The `dimension environment` specifies things like: `bed explosion?`, `infinite burning?`, `has skylight?`...
        """)
    @SerializedName(value = "dimension_type", alternate = "dimensionType")
    public String dimension_type = null;

    @Document(id = 1752729741419L, value = """
        The `chunk generator` of this `dimension`.
        Note that in vanilla Minecraft, the `chunk generator` of `minecraft:overworld`, `minecraft:the_nether` and `minecraft:the_end` are all `NoiseChunkGenerator`.

        <green>For a non-vanilla chunk generator provided by mods, you can just specify this value to `NOISE`.
        <green>If this value is specified to `NOISE`, then we will just use whatever the chunk generator that mod is using.
        """)
    public ChunkGeneratorType chunkGeneratorType = ChunkGeneratorType.NOISE;

    @Document(id = 1752734540525L, value = """
        The `parameters` used by the `chunk generator`.

        For `flat chunk generator`, you can specify the `preset string` as the `parameters`.
        """)
    public String chunkGeneratorParameters = "";

    @Document(id = 1752246679197L, value = """
        The `seed` used for the `chunk generator` of this dimension.
        """)
    public long seed;

    public Difficulty difficulty = Difficulty.NORMAL;

    @Document(id = 1752246657296L, value = """
        Should we tick the time of this `dimension`? (Do the day night cycle?)
        """)
    public boolean shouldTickTime;

    @Document(id = 1752286206946L, value = """
        The equivalent to `DayTime` in `level.dat`.
        """)
    public long timeOfDay = 6000;

    public Weather weather = new Weather();
    public static class Weather {
        public int sunnyTime;
        public boolean isRaining;
        public int rainTime;
        public boolean isThundering;
        public int thunderTime;
    }

    public boolean isDimensionLoaded() {
        return ServerHelper
            .getWorlds()
            .stream()
            .anyMatch(it -> RegistryHelper.toIdString(it).equals(this.dimension));
    }

    public boolean isDebugWorld() {
        return WorldPresetType.DEBUG_ALL_BLOCK_STATES == this.worldPresetType;
    }

    public Optional<ServerWorld> getLoadedWorld() {
        return ServerHelper.getWorld(this.dimension);
    }
}

package mod.fuji.module.initializer.world.manager.command.argument.wrapper;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.presets.WorldPresets;

public enum WorldPresetType {
    DEFAULT(WorldPresets.NORMAL)
    , FLAT(WorldPresets.FLAT)
    , LARGE_BIOMES(WorldPresets.LARGE_BIOMES)
    , AMPLIFIED(WorldPresets.AMPLIFIED)
    , SINGLE_BIOME_SURFACE(WorldPresets.SINGLE_BIOME_SURFACE)
    , DEBUG_ALL_BLOCK_STATES(WorldPresets.DEBUG);

    @SuppressWarnings("ImmutableEnumChecker")
    private final ResourceKey<WorldPreset> worldPresetKey;

    WorldPresetType(ResourceKey<WorldPreset> worldPresetKey) {
        this.worldPresetKey = worldPresetKey;
    }

    public ResourceKey<WorldPreset> toWorldPresetKey() {
        return this.worldPresetKey;
    }

}

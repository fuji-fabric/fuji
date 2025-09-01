package io.github.sakurawald.fuji.module.initializer.world.manager.command.argument.wrapper;

import net.minecraft.registry.RegistryKey;
import net.minecraft.world.gen.WorldPreset;
import net.minecraft.world.gen.WorldPresets;

public enum WorldPresetType {
    DEFAULT(WorldPresets.DEFAULT)
    , FLAT(WorldPresets.FLAT)
    , LARGE_BIOMES(WorldPresets.LARGE_BIOMES)
    , AMPLIFIED(WorldPresets.AMPLIFIED)
    , SINGLE_BIOME_SURFACE(WorldPresets.SINGLE_BIOME_SURFACE)
    , DEBUG_ALL_BLOCK_STATES(WorldPresets.DEBUG_ALL_BLOCK_STATES);

    @SuppressWarnings("ImmutableEnumChecker")
    private final RegistryKey<WorldPreset> worldPresetKey;

    WorldPresetType(RegistryKey<WorldPreset> worldPresetKey) {
        this.worldPresetKey = worldPresetKey;
    }

    public RegistryKey<WorldPreset> toWorldPresetKey() {
        return this.worldPresetKey;
    }

}

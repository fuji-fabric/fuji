package io.github.sakurawald.fuji.module.initializer.world.structure;

import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.level.UnmodifiableLevelProperties;
import org.jetbrains.annotations.NotNull;

/**
 * The only purpose of this class is to warp the seed.
 **/
public final class RuntimeWorldProperties extends UnmodifiableLevelProperties {

    public final DimensionNode dimensionNode;
    private GameRules gameRules;

    public RuntimeWorldProperties(@NotNull SaveProperties saveProperties, DimensionNode dimensionNode) {
        super(saveProperties, saveProperties.getMainWorldProperties());
        this.dimensionNode = dimensionNode;
    }

    @Override
    public Difficulty getDifficulty() {
        return dimensionNode.difficulty;
    }

    @Override
    public long getTimeOfDay() {
        return dimensionNode.timeOfDay;
    }
}

package io.github.sakurawald.fuji.module.initializer.world.structure;

import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.fuji.module.initializer.world.service.WorldService;
import java.util.Optional;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.level.UnmodifiableLevelProperties;
import org.jetbrains.annotations.NotNull;

public final class RuntimeWorldProperties extends UnmodifiableLevelProperties {

    private final SaveProperties saveProperties;
    public @NotNull DimensionNode dimensionNode;

    private GameRules gameRules;

    public RuntimeWorldProperties(@NotNull SaveProperties saveProperties, @NotNull DimensionNode dimensionNode) {
        super(saveProperties, saveProperties.getMainWorldProperties());
        this.saveProperties = saveProperties;
        this.dimensionNode = dimensionNode;

        applyDimensionNode(dimensionNode);
    }

    private void applyDimensionNode(DimensionNode dimensionNode) {
        applyGameRules(dimensionNode);
    }

    private void applyGameRules(DimensionNode dimensionNode) {
        this.gameRules = new GameRules(this.saveProperties.getEnabledFeatures());
        dimensionNode.gameRules.applyTo(this.gameRules, ServerHelper.getServer());
    }

    private DimensionNode getEffectiveDimensionNode() {
        /* Detect the changes of dimension node in storage. */
        DimensionNode originalDimensionNode = dimensionNode;
        Optional<DimensionNode> newDimensionNodeOpt = WorldService.getDimensionNode(originalDimensionNode.dimension);
        if (newDimensionNodeOpt.isPresent()) {
            DimensionNode newDimensionNode = newDimensionNodeOpt.get();
            if (originalDimensionNode != newDimensionNode) {
                LogUtil.info("The config for dimension {} is modified, I will apply the new config now!", originalDimensionNode.dimension);
                this.dimensionNode = newDimensionNode;
                this.applyDimensionNode(this.dimensionNode);
            }
        }

        /* Return the effective dimension node. */
        return this.dimensionNode;
    }

    @Override
    public Difficulty getDifficulty() {
        return getEffectiveDimensionNode().difficulty;
    }


    @Override
    public GameRules getGameRules() {
        return this.gameRules;
    }

    @Override
    public long getTime() {
        // NOTE: We just mirror the `Time` in `level.dat`.
        return super.getTime();
    }

    @Override
    public long getTimeOfDay() {
        return this.getEffectiveDimensionNode().timeOfDay;
    }

    @Override
    public void setTimeOfDay(long l) {
        this.getEffectiveDimensionNode().timeOfDay = l;
    }

    @Override
    public boolean isRaining() {
        return this.getEffectiveDimensionNode().weather.isRaining;
    }

    @Override
    public void setRaining(boolean bl) {
        this.getEffectiveDimensionNode().weather.isRaining = bl;
    }

    @Override
    public int getRainTime() {
        return this.getEffectiveDimensionNode().weather.rainTime;
    }

    @Override
    public void setRainTime(int i) {
        this.getEffectiveDimensionNode().weather.rainTime = i;
    }

    @Override
    public boolean isThundering() {
        return this.getEffectiveDimensionNode().weather.isThundering;
    }

    @Override
    public void setThundering(boolean bl) {
        this.getEffectiveDimensionNode().weather.isThundering = bl;
    }

    @Override
    public int getThunderTime() {
        return this.getEffectiveDimensionNode().weather.thunderTime;
    }

    @Override
    public void setThunderTime(int i) {
        this.getEffectiveDimensionNode().weather.thunderTime = i;
    }

    @Override
    public int getClearWeatherTime() {
        return this.getEffectiveDimensionNode().weather.sunnyTime;
    }

    @Override
    public void setClearWeatherTime(int i) {
        this.getEffectiveDimensionNode().weather.sunnyTime = i;
    }

}

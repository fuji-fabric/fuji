package io.github.sakurawald.fuji.module.initializer.world.structure;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.ServerHelper;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.level.UnmodifiableLevelProperties;
import org.jetbrains.annotations.NotNull;

public final class RuntimeWorldProperties extends UnmodifiableLevelProperties {

    public DimensionNode dimensionNode;
    private GameRules gameRules;

    public RuntimeWorldProperties(@NotNull SaveProperties saveProperties, DimensionNode dimensionNode) {
        super(saveProperties, saveProperties.getMainWorldProperties());
        this.dimensionNode = dimensionNode;

        applyGameRules(saveProperties, dimensionNode);
    }

    private void applyGameRules(@NotNull SaveProperties saveProperties, DimensionNode dimensionNode) {
        this.gameRules = new GameRules(saveProperties.getEnabledFeatures());
        dimensionNode.gameRules.applyTo(this.gameRules, ServerHelper.getServer());
    }

    @Override
    public Difficulty getDifficulty() {
        return dimensionNode.difficulty;
    }


    @Override
    public GameRules getGameRules() {
//        LogUtil.info("getGameRules(): {}", this.gameRules);
        return this.gameRules;
    }


    @Override
    public WorldBorder.Properties getWorldBorder() {
        return dimensionNode.worldBorder;
    }

    @Override
    public void setWorldBorder(WorldBorder.Properties properties) {
        dimensionNode.worldBorder = properties;
    }

    @Override
    public long getTimeOfDay() {
        return dimensionNode.timeOfDay;
    }

    @Override
    public void setTimeOfDay(long l) {
        this.dimensionNode.timeOfDay = l;
    }

    @Override
    public boolean isRaining() {
        return dimensionNode.isRaining;
    }

    @Override
    public void setRaining(boolean bl) {
        this.dimensionNode.isRaining = bl;
    }

    @Override
    public int getRainTime() {
        return dimensionNode.rainTime;
    }

    @Override
    public void setRainTime(int i) {
        this.dimensionNode.rainTime = i;
    }

    @Override
    public boolean isThundering() {
        return dimensionNode.isThundering;
    }

    @Override
    public void setThundering(boolean bl) {
        this.dimensionNode.isThundering = bl;
    }

    @Override
    public int getThunderTime() {
        return dimensionNode.thunderTime;
    }

    @Override
    public void setThunderTime(int i) {
        this.dimensionNode.thunderTime = i;
    }

    @Override
    public int getClearWeatherTime() {
        return dimensionNode.sunnyTime;
    }

    @Override
    public void setClearWeatherTime(int i) {
        this.dimensionNode.sunnyTime = i;
    }
}

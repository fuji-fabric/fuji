package io.github.sakurawald.fuji.module.initializer.world.runtime.structure;

import net.minecraft.world.Difficulty;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.level.UnmodifiableLevelProperties;
import org.jetbrains.annotations.NotNull;

public final class RuntimeDimensionProperties extends UnmodifiableLevelProperties {

    public RuntimeDimensionDescriptor runtimeDimensionDescriptor;

    public RuntimeDimensionProperties(@NotNull SaveProperties saveProperties, @NotNull RuntimeDimensionDescriptor runtimeDimensionDescriptor) {
        super(saveProperties, saveProperties.getMainWorldProperties());
        this.runtimeDimensionDescriptor = runtimeDimensionDescriptor;
    }

    @Override
    public Difficulty getDifficulty() {
        return this.runtimeDimensionDescriptor.difficulty;
    }

    @Override
    public long getTime() {
        // NOTE: We just mirror the `Time` in `level.dat`.
        return super.getTime();
    }

    @Override
    public long getTimeOfDay() {
        return this.runtimeDimensionDescriptor.timeOfDay;
    }

    @Override
    public void setTimeOfDay(long l) {
        this.runtimeDimensionDescriptor.timeOfDay = l;
    }

    @Override
    public boolean isRaining() {
        return this.runtimeDimensionDescriptor.weather.isRaining;
    }

    @Override
    public void setRaining(boolean bl) {
        this.runtimeDimensionDescriptor.weather.isRaining = bl;
    }

    @Override
    public int getRainTime() {
        return this.runtimeDimensionDescriptor.weather.rainTime;
    }

    @Override
    public void setRainTime(int i) {
        this.runtimeDimensionDescriptor.weather.rainTime = i;
    }

    @Override
    public boolean isThundering() {
        return this.runtimeDimensionDescriptor.weather.isThundering;
    }

    @Override
    public void setThundering(boolean bl) {
        this.runtimeDimensionDescriptor.weather.isThundering = bl;
    }

    @Override
    public int getThunderTime() {
        return this.runtimeDimensionDescriptor.weather.thunderTime;
    }

    @Override
    public void setThunderTime(int i) {
        this.runtimeDimensionDescriptor.weather.thunderTime = i;
    }

    @Override
    public int getClearWeatherTime() {
        return this.runtimeDimensionDescriptor.weather.sunnyTime;
    }

    @Override
    public void setClearWeatherTime(int i) {
        this.runtimeDimensionDescriptor.weather.sunnyTime = i;
    }

}

package io.github.sakurawald.fuji.module.initializer.world.manager.structure;

import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.module.initializer.world.manager.service.WorldService;
import java.util.Optional;
import net.minecraft.world.Difficulty;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.level.UnmodifiableLevelProperties;
import org.jetbrains.annotations.NotNull;

public final class RuntimeDimensionProperties extends UnmodifiableLevelProperties {

    private RuntimeDimensionDescriptor runtimeDimensionDescriptor;

    public RuntimeDimensionProperties(@NotNull SaveProperties saveProperties, @NotNull RuntimeDimensionDescriptor runtimeDimensionDescriptor) {
        super(saveProperties, saveProperties.getMainWorldProperties());
        this.runtimeDimensionDescriptor = runtimeDimensionDescriptor;
    }

    public RuntimeDimensionDescriptor getEffectiveRuntimeDimensionDescriptor() {
        /* Ensure the runtime dimension descriptor is the latest version. */
        Optional<RuntimeDimensionDescriptor> newValueOpt = WorldService.getRuntimeDimensionDescriptor(this.runtimeDimensionDescriptor.dimension);
        if (newValueOpt.isPresent()) {
            RuntimeDimensionDescriptor newValue = newValueOpt.get();
            if (!this.runtimeDimensionDescriptor.equals(newValue)) {
                tryFixRuntimeDimensionDescriptor(newValue);
                this.runtimeDimensionDescriptor = newValue;
                LogUtil.info("Apply latest version of runtime dimension descriptor: {}", newValue.dimension);
            }
        }

        return this.runtimeDimensionDescriptor;
    }

    private void tryFixRuntimeDimensionDescriptor(RuntimeDimensionDescriptor newValue) {
        if (newValue.difficulty == null) {
            newValue.difficulty = Difficulty.NORMAL;
        }
    }

    @Override
    public Difficulty getDifficulty() {
        return this.getEffectiveRuntimeDimensionDescriptor().difficulty;
    }

    @Override
    public long getTime() {
        // NOTE: We just mirror the `Time` in `level.dat`.
        return super.getTime();
    }

    @Override
    public long getTimeOfDay() {
        return this.getEffectiveRuntimeDimensionDescriptor().timeOfDay;
    }

    @Override
    public void setTimeOfDay(long l) {
        this.getEffectiveRuntimeDimensionDescriptor().timeOfDay = l;
    }

    @Override
    public boolean isRaining() {
        return this.getEffectiveRuntimeDimensionDescriptor().weather.isRaining;
    }

    @Override
    public void setRaining(boolean bl) {
        this.getEffectiveRuntimeDimensionDescriptor().weather.isRaining = bl;
    }

    @Override
    public int getRainTime() {
        return this.getEffectiveRuntimeDimensionDescriptor().weather.rainTime;
    }

    @Override
    public void setRainTime(int i) {
        this.getEffectiveRuntimeDimensionDescriptor().weather.rainTime = i;
    }

    @Override
    public boolean isThundering() {
        return this.getEffectiveRuntimeDimensionDescriptor().weather.isThundering;
    }

    @Override
    public void setThundering(boolean bl) {
        this.getEffectiveRuntimeDimensionDescriptor().weather.isThundering = bl;
    }

    @Override
    public int getThunderTime() {
        return this.getEffectiveRuntimeDimensionDescriptor().weather.thunderTime;
    }

    @Override
    public void setThunderTime(int i) {
        this.getEffectiveRuntimeDimensionDescriptor().weather.thunderTime = i;
    }

    @Override
    public int getClearWeatherTime() {
        return this.getEffectiveRuntimeDimensionDescriptor().weather.sunnyTime;
    }

    @Override
    public void setClearWeatherTime(int i) {
        this.getEffectiveRuntimeDimensionDescriptor().weather.sunnyTime = i;
    }

}

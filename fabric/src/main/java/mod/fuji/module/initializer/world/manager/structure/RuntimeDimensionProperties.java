package mod.fuji.module.initializer.world.manager.structure;

import mod.fuji.core.auxiliary.LogUtil;
import mod.fuji.module.initializer.world.manager.service.WorldService;
import java.util.Optional;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.storage.WorldData;
import net.minecraft.world.level.storage.DerivedLevelData;
import org.jetbrains.annotations.NotNull;

public final class RuntimeDimensionProperties extends DerivedLevelData {

    private RuntimeDimensionDescriptor runtimeDimensionDescriptor;

    public RuntimeDimensionProperties(@NotNull WorldData saveProperties, @NotNull RuntimeDimensionDescriptor runtimeDimensionDescriptor) {
        super(saveProperties, saveProperties.overworldData());
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
    public long getGameTime() {
        // NOTE: We just mirror the `Time` in `level.dat`.
        return super.getGameTime();
    }

    @Override
    public long getDayTime() {
        return this.getEffectiveRuntimeDimensionDescriptor().timeOfDay;
    }

    @Override
    public void setDayTime(long l) {
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

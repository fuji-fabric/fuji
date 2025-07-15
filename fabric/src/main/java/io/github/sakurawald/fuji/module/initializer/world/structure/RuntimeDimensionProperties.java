package io.github.sakurawald.fuji.module.initializer.world.structure;

import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.fuji.module.initializer.world.service.WorldService;
import io.github.sakurawald.fuji.module.initializer.world.structure.gamerule.GameRuleStore;
import java.util.Optional;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.level.UnmodifiableLevelProperties;
import org.jetbrains.annotations.NotNull;

public final class RuntimeDimensionProperties extends UnmodifiableLevelProperties {

    private RuntimeDimensionDescriptor runtimeDimensionDescriptor;

    private GameRules gameRules;

    public RuntimeDimensionProperties(@NotNull SaveProperties saveProperties, @NotNull RuntimeDimensionDescriptor runtimeDimensionDescriptor) {
        super(saveProperties, saveProperties.getMainWorldProperties());
        this.changeRuntimeDimensionDescriptor(runtimeDimensionDescriptor);
    }

    private void applyRuntimeDimensionDescriptor() {
        this.applyGameRules();
    }

    private void applyGameRules() {
        this.gameRules = GameRuleStore.makeGameRules();
        getEffectiveRuntimeDimensionDescriptor().gameRules.applyTo(this.gameRules, ServerHelper.getServer());
    }

    public RuntimeDimensionDescriptor getEffectiveRuntimeDimensionDescriptor() {
        /* Detect the changes of dimension descriptor in storage. */
        RuntimeDimensionDescriptor originalRuntimeDimensionDescriptor = this.runtimeDimensionDescriptor;
        Optional<RuntimeDimensionDescriptor> newDimensionDescriptorOptional = WorldService.getDimensionDescriptor(originalRuntimeDimensionDescriptor.dimension);
        if (newDimensionDescriptorOptional.isPresent()) {
            RuntimeDimensionDescriptor newRuntimeDimensionDescriptor = newDimensionDescriptorOptional.get();
            if (originalRuntimeDimensionDescriptor != newRuntimeDimensionDescriptor) {
                LogUtil.info("The config for dimension {} is modified, I will apply the new config now!", originalRuntimeDimensionDescriptor.dimension);
                this.changeRuntimeDimensionDescriptor(newRuntimeDimensionDescriptor);
            }
        }

        /* Return the effective dimension node. */
        return this.runtimeDimensionDescriptor;
    }

    private void changeRuntimeDimensionDescriptor(RuntimeDimensionDescriptor newRuntimeDimensionDescriptor) {
        this.runtimeDimensionDescriptor = newRuntimeDimensionDescriptor;
        this.applyRuntimeDimensionDescriptor();
    }


    @Override
    public Difficulty getDifficulty() {
        return getEffectiveRuntimeDimensionDescriptor().difficulty;
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

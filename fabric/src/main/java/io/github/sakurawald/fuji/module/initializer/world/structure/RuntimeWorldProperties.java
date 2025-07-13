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

public final class RuntimeWorldProperties extends UnmodifiableLevelProperties {

    private final SaveProperties saveProperties;
    public @NotNull RuntimeWorldDescriptor runtimeWorldDescriptor;

    private GameRules gameRules;

    public RuntimeWorldProperties(@NotNull SaveProperties saveProperties, @NotNull RuntimeWorldDescriptor runtimeWorldDescriptor) {
        super(saveProperties, saveProperties.getMainWorldProperties());
        this.saveProperties = saveProperties;
        this.runtimeWorldDescriptor = runtimeWorldDescriptor;

        applyDimensionNode(runtimeWorldDescriptor);
    }

    private void applyDimensionNode(RuntimeWorldDescriptor runtimeWorldDescriptor) {
        applyGameRules(runtimeWorldDescriptor);
    }

    private void applyGameRules(RuntimeWorldDescriptor runtimeWorldDescriptor) {
        this.gameRules = GameRuleStore.makeGameRules();
        runtimeWorldDescriptor.gameRules.applyTo(this.gameRules, ServerHelper.getServer());
    }

    private RuntimeWorldDescriptor getEffectiveDimensionNode() {
        /* Detect the changes of dimension node in storage. */
        RuntimeWorldDescriptor originalRuntimeWorldDescriptor = runtimeWorldDescriptor;
        Optional<RuntimeWorldDescriptor> newDimensionNodeOpt = WorldService.getDimensionNode(originalRuntimeWorldDescriptor.dimension);
        if (newDimensionNodeOpt.isPresent()) {
            RuntimeWorldDescriptor newRuntimeWorldDescriptor = newDimensionNodeOpt.get();
            if (originalRuntimeWorldDescriptor != newRuntimeWorldDescriptor) {
                LogUtil.info("The config for dimension {} is modified, I will apply the new config now!", originalRuntimeWorldDescriptor.dimension);
                this.runtimeWorldDescriptor = newRuntimeWorldDescriptor;
                this.applyDimensionNode(this.runtimeWorldDescriptor);
            }
        }

        /* Return the effective dimension node. */
        return this.runtimeWorldDescriptor;
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

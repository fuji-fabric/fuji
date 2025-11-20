package mod.fuji.module.mixin.world.gamerule;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import mod.fuji.core.annotation.HotPath;
import mod.fuji.core.auxiliary.minecraft.RegistryHelper;
import mod.fuji.module.initializer.world.gamerule.WorldGameRuleInitializer;

#if MC_VER >= MC_1_21_3
import net.minecraft.server.level.ServerLevel;
#endif

import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

#if MC_VER < MC_1_21_3
@Mixin(Level.class)
#elif MC_VER >= MC_1_21_3
@Mixin(ServerLevel.class)
#endif
public abstract class PerDimensionGameRulesMixin {

    @Unique
    @HotPath
    private String dimensionId;

    @ModifyReturnValue(method = "getGameRules", at = @At("RETURN"))
    GameRules modifyPerDimensionGameRules(GameRules original) {
        if (dimensionId == null) {
            Level world = (Level) (Object) this;
            dimensionId = RegistryHelper.getIdAsString(world);
        }

        return WorldGameRuleInitializer.getEffectiveGameRules(dimensionId, original);
    }
}

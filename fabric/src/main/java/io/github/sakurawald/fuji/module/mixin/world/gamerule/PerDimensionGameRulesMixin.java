package io.github.sakurawald.fuji.module.mixin.world.gamerule;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.sakurawald.fuji.core.annotation.HotPath;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.RegistryHelper;
import io.github.sakurawald.fuji.module.initializer.world.gamerule.WorldGameRuleInitializer;

#if MC_VER >= MC_1_21_3
import net.minecraft.server.world.ServerWorld;
#endif

import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

#if MC_VER < MC_1_21_3
@Mixin(World.class)
#elif MC_VER >= MC_1_21_3
@Mixin(ServerWorld.class)
#endif
public abstract class PerDimensionGameRulesMixin {

    @Unique
    @HotPath
    private String dimensionId;

    @ModifyReturnValue(method = "getGameRules", at = @At("RETURN"))
    GameRules modifyPerDimensionGameRules(GameRules original) {
        if (dimensionId == null) {
            World world = (World) (Object) this;
            dimensionId = RegistryHelper.getIdAsString(world);
        }

        return WorldGameRuleInitializer.getEffectiveGameRules(dimensionId, original);
    }
}

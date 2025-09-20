package io.github.sakurawald.fuji.module.mixin.world.border;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.sakurawald.fuji.core.annotation.HotPath;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.RegistryHelper;
import io.github.sakurawald.fuji.module.initializer.world.border.WorldBorderInitializer;
import io.github.sakurawald.fuji.module.initializer.world.border.structure.BorderDescriptor;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

#if MC_VER < MC_1_21_9
@Mixin(World.class)
#elif MC_VER >= MC_1_21_9
@Mixin(ServerWorld.class)
#endif
public class BorderMixin {

    @Unique
    @HotPath
    private String dimensionId;

    @ModifyReturnValue(method = "getWorldBorder", at = @At("RETURN"))
    WorldBorder modifyTheReturnValueOfGetWorldBorder(WorldBorder original) {
        if (dimensionId == null) {
            World world = (World) (Object) this;
            dimensionId = RegistryHelper.getIdAsString(world);
        }

        return WorldBorderInitializer
            .getEffectiveBorderDescriptor(dimensionId)
            .map(BorderDescriptor::asVanillaWorldBorder)
            .orElse(original);
    }
}

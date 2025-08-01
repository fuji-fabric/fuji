package io.github.sakurawald.fuji.module.mixin.world.border;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.RegistryHelper;
import io.github.sakurawald.fuji.module.initializer.world.border.WorldBorderInitializer;
import io.github.sakurawald.fuji.module.initializer.world.border.structure.BorderDescriptor;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(World.class)
public class WorldMixin {

    @ModifyReturnValue(method = "getWorldBorder", at = @At("RETURN"))
    WorldBorder modifyTheReturnValueOfGetWorldBorder(WorldBorder original) {
        World world = (World) (Object) this;
        String dimensionId = RegistryHelper.getIdAsString(world);

        return WorldBorderInitializer
            .getEffectiveBorderDescriptor(dimensionId)
            .map(BorderDescriptor::asVanillaWorldBorder)
            .orElse(original);
    }
}

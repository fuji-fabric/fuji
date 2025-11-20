package mod.fuji.module.mixin.world.border;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import mod.fuji.core.annotation.HotPath;
import mod.fuji.core.auxiliary.minecraft.RegistryHelper;
import mod.fuji.module.initializer.world.border.WorldBorderInitializer;
import mod.fuji.module.initializer.world.border.structure.BorderDescriptor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

#if MC_VER < MC_1_21_9
@Mixin(Level.class)
#elif MC_VER >= MC_1_21_9
@Mixin(net.minecraft.server.level.ServerLevel.class)
#endif
public class BorderMixin {

    @Unique
    @HotPath
    private String dimensionId;

    @ModifyReturnValue(method = "getWorldBorder", at = @At("RETURN"))
    WorldBorder modifyTheReturnValueOfGetWorldBorder(WorldBorder original) {
        if (dimensionId == null) {
            Level world = (Level) (Object) this;
            dimensionId = RegistryHelper.getIdAsString(world);
        }

        return WorldBorderInitializer
            .getEffectiveBorderDescriptor(dimensionId)
            .map(BorderDescriptor::asVanillaWorldBorder)
            .orElse(original);
    }
}

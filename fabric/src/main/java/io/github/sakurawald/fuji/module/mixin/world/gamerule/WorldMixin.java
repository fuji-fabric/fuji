package io.github.sakurawald.fuji.module.mixin.world.gamerule;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.RegistryHelper;
import io.github.sakurawald.fuji.module.initializer.world.gamerule.WorldGameRuleInitializer;
import io.github.sakurawald.fuji.module.initializer.world.gamerule.structure.GameRuleDescriptor;
import java.util.Optional;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(World.class)
public abstract class WorldMixin{

    @ModifyReturnValue(method = "getGameRules", at = @At("RETURN"))
    GameRules modifyPerDimensionGameRules(GameRules original) {
        World world = (World) (Object) this;
        String dimensionId = RegistryHelper.toString(world);
        Optional<GameRuleDescriptor> effectiveGameRuleDescriptor = WorldGameRuleInitializer
            .getEffectiveGameRuleDescriptor(dimensionId);
        return effectiveGameRuleDescriptor
            .map(GameRuleDescriptor::asVanillaGameRules)
            .orElse(original);
    }
}

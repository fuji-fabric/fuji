package mod.fuji.module.mixin.anti_build;

import mod.fuji.core.auxiliary.minecraft.RegistryHelper;
import mod.fuji.module.initializer.anti_build.AntiBuildInitializer;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

#if MC_VER < MC_26_1
@Mixin(ServerPlayer.class)
#elif MC_VER >= MC_26_1
@Mixin(Player.class)
#endif
public class PlayerAttackEntityMixin {

    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    void handleAttackEntity(Entity entity, CallbackInfo ci) {
        var config = AntiBuildInitializer.config.model().getAntiActionTypes().getAttackEntity();
        if (!config.isEnable()) return;

        ServerPlayer serverPlayerEntity = (ServerPlayer) (Object) this;
        String id = RegistryHelper.getIdAsString(entity);
        AntiBuildInitializer.processAntiBuildAction(serverPlayerEntity, "attack_entity", config.getId(), id, ci::cancel, () -> true);
    }

}

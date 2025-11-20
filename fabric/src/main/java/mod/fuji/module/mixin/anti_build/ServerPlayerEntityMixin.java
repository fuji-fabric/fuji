package mod.fuji.module.mixin.anti_build;

import mod.fuji.core.auxiliary.minecraft.RegistryHelper;
import mod.fuji.module.initializer.anti_build.AntiBuildInitializer;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class ServerPlayerEntityMixin {

    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    void handleAttackEntity(Entity entity, CallbackInfo ci) {
        var config = AntiBuildInitializer.config.model().getAntiTypes().getAttackEntity();
        if (!config.isEnable()) return;

        ServerPlayer serverPlayerEntity = (ServerPlayer) (Object) this;
        String id = RegistryHelper.getIdAsString(entity);
        AntiBuildInitializer.processAntiBuild(serverPlayerEntity, "attack_entity", config.getId(), id, ci::cancel, () -> true);
    }

}

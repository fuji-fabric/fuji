package io.github.sakurawald.fuji.module.mixin.anti_build;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.RegistryHelper;
import io.github.sakurawald.fuji.module.initializer.anti_build.AntiBuildInitializer;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {

    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    void f(Entity entity, CallbackInfo ci) {
        ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity) (Object) this;
        String id = RegistryHelper.getIdAsString(entity);
        AntiBuildInitializer.processAntiBuild(serverPlayerEntity, "attack_entity", AntiBuildInitializer.config.model().anti.attack_entity.id, id, ci::cancel, () -> true);
    }

}

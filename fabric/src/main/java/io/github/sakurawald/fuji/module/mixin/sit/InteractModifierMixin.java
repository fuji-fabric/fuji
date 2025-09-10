package io.github.sakurawald.fuji.module.mixin.sit;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Copyright 2021 BradBot_1
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@Mixin(ServerPlayerInteractionManager.class)
public class InteractModifierMixin {

    @Final
    @Shadow
    protected ServerPlayerEntity player;

    @Inject(method = "setGameMode(Lnet/minecraft/world/GameMode;Lnet/minecraft/world/GameMode;)V", at = @At("HEAD"))
    public void getOutTheChairIfYouAreSpectator(GameMode gameMode, GameMode previousGameMode, CallbackInfo callbackInfo) {
        if (gameMode == GameMode.SPECTATOR && previousGameMode != GameMode.SPECTATOR
            && player.getVehicle() != null) {
            PlayerHelper.dismountRidingEntity(player);
        }
    }

}

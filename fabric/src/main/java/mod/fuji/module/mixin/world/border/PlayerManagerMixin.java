package mod.fuji.module.mixin.world.border;

import com.llamalad7.mixinextras.sugar.Local;
import mod.fuji.core.auxiliary.minecraft.RegistryHelper;
import mod.fuji.module.initializer.world.border.WorldBorderInitializer;
import mod.fuji.module.initializer.world.border.structure.BorderDescriptor;
import mod.fuji.module.initializer.world.border.structure.PerDimensionWorldBorderListener;
import net.minecraft.server.players.PlayerList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.border.BorderChangeListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public class PlayerManagerMixin {

    @ModifyArg(method = "sendLevelInfo", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/game/ClientboundInitializeBorderPacket;<init>(Lnet/minecraft/world/level/border/WorldBorder;)V", ordinal = 0))
    WorldBorder modifyWorldBorderInitializePacket(WorldBorder original, @Local(argsOnly = true) ServerLevel serverWorld) {
        return WorldBorderInitializer
            .getEffectiveBorderDescriptor(RegistryHelper.getIdAsString(serverWorld))
            .map(BorderDescriptor::asVanillaWorldBorder)
            .orElse(original);
    }

    @Inject(method = "sendLevelInfo", at = @At("TAIL"))
    void ensureClientSideWorldBorderIsSyncedAfterTeleport(ServerPlayer player, ServerLevel serverWorld, CallbackInfo ci) {
        WorldBorderInitializer.sendWorldBorderSyncPacketsToPlayer(player, serverWorld);
    }

    @ModifyArg(method = "addWorldborderListener", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/border/WorldBorder;addListener(Lnet/minecraft/world/level/border/BorderChangeListener;)V", ordinal = 0))
    BorderChangeListener registerPerDimensionWorldBorderListener(BorderChangeListener original, @Local(argsOnly = true) ServerLevel serverWorld) {
        return new PerDimensionWorldBorderListener();
    }

}

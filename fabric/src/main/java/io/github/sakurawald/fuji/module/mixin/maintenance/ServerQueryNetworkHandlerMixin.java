package io.github.sakurawald.fuji.module.mixin.maintenance;

import io.github.sakurawald.fuji.module.initializer.maintenance.service.MaintenanceService;
import java.util.Optional;
import net.minecraft.server.ServerMetadata;
import net.minecraft.server.network.ServerQueryNetworkHandler;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = ServerQueryNetworkHandler.class, priority = 2000)
public abstract class ServerQueryNetworkHandlerMixin {

    @ModifyArg(method = "onRequest", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/packet/s2c/query/QueryResponseS2CPacket;<init>(Lnet/minecraft/server/ServerMetadata;)V"))
    public @NotNull ServerMetadata handleQueryRequest(ServerMetadata original) {
        if (!MaintenanceService.getMaintenanceModeStatus()) {
            return original;
        }

        Text text = MaintenanceService.getEffectiveMaintenanceMessageText();
        Optional<ServerMetadata.Players> players = original.comp_1274();
        Optional<ServerMetadata.Version> version = original.comp_1275();
        Optional<ServerMetadata.Favicon> icon = original.comp_1276();
        return new ServerMetadata(text, players, version, icon, original.secureChatEnforced());
    }

}

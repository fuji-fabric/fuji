package io.github.sakurawald.core.auxiliary.minecraft;

import com.mojang.authlib.GameProfile;
import lombok.experimental.UtilityClass;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

@UtilityClass
public class PlayerHelper {

    public static ServerPlayerEntity makePlayer(GameProfile gameProfile) {
        MinecraftServer server = ServerHelper.getServer();
        SyncedClientOptions syncedClientOptions = SyncedClientOptions.createDefault();
        return new ServerPlayerEntity(server, server.getOverworld(), gameProfile, syncedClientOptions);
    }

}

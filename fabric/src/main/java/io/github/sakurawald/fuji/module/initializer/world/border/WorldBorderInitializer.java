package io.github.sakurawald.fuji.module.initializer.world.border;

import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.RegistryHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.world.border.config.model.WorldBorderConfigModel;
import io.github.sakurawald.fuji.module.initializer.world.border.structure.BorderDescriptor;
import java.util.Optional;
import net.minecraft.network.packet.s2c.play.WorldBorderCenterChangedS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderSizeChangedS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderWarningBlocksChangedS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderWarningTimeChangedS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.border.WorldBorder;

@Document(id = 1752561532728L, value = """
    This module allows you to customize the `per-dimension world border`.
    """)
public class WorldBorderInitializer extends ModuleInitializer {

    public static BaseConfigurationHandler<WorldBorderConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, WorldBorderConfigModel.class);

    public static Optional<BorderDescriptor> getBorderDescriptor(String dimensionId) {
        return config.model().borders
            .stream()
            .filter(it -> it.dimensionId.equals(dimensionId))
            .findFirst();
    }

    public static void syncWorldBorder() {
        ServerHelper
            .getOnlinePlayers()
            .forEach(WorldBorderInitializer::syncWorldBorder);
    }

    public static void syncWorldBorder(ServerPlayerEntity player) {
        ServerWorld world = PlayerHelper.getServerWorld(player);
        WorldBorder worldBorder = world.getWorldBorder();

        LogUtil.debug("Sync world border: player = {}, world = {}, size = {}", PlayerHelper.getPlayerName(player), RegistryHelper.toString(world), worldBorder.getSize());
        player.networkHandler.sendPacket(new WorldBorderCenterChangedS2CPacket(worldBorder));
        player.networkHandler.sendPacket(new WorldBorderSizeChangedS2CPacket(worldBorder));
        player.networkHandler.sendPacket(new WorldBorderWarningBlocksChangedS2CPacket(worldBorder));
        player.networkHandler.sendPacket(new WorldBorderWarningTimeChangedS2CPacket(worldBorder));
//        player.networkHandler.sendPacket(new WorldBorderInterpolateSizeS2CPacket(worldBorder));
    }
}

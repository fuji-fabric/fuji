package io.github.sakurawald.fuji.module.initializer.world.border;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.world.border.config.model.WorldBorderConfigModel;
import io.github.sakurawald.fuji.module.initializer.world.border.structure.BorderDescriptor;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.WorldBorderCenterChangedS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderInterpolateSizeS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderSizeChangedS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderWarningBlocksChangedS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderWarningTimeChangedS2CPacket;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;

@Document(id = 1752561532728L, value = """
    This module allows you to customize the `per-dimension border`.
    """)
@ColorBox(id = 1752460350802L, color = ColorBox.ColorBlockTypes.TIPS, value = """
    ◉ The logic of `/worldborder` command.
    The `/worldborder` command only sets the `World Border` of `minecraft:overworld`.
    But the `minecraft:the_nether` and `minecraft:the_end` dimensions will `sync` the `world border` of `minecraft:overworld`.
    """)
@ColorBox(id = 1752569349615L, color = ColorBox.ColorBlockTypes.TIPS, value = """
    ◉ The semantics of options for `world border`.
    See: https://minecraft.wiki/w/World_border
    """)
public class WorldBorderInitializer extends ModuleInitializer {

    private static final BaseConfigurationHandler<WorldBorderConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, WorldBorderConfigModel.class);

    public static Optional<BorderDescriptor> getEffectiveBorderDescriptor(String dimensionId) {
        return config.model().borders
            .stream()
            .filter(it -> it.enable
                && it.dimensionId.equals(dimensionId))
            .findFirst();
    }

    @Override
    protected void onReload() {
        sendPerDimensionWorldBorderPackets();
    }

    private static void sendPerDimensionWorldBorderPackets() {
        sendPerDimensionWorldBorderPacket(dimension -> new WorldBorderCenterChangedS2CPacket(dimension.getWorldBorder()));
        sendPerDimensionWorldBorderPacket(dimension -> new WorldBorderSizeChangedS2CPacket(dimension.getWorldBorder()));
        sendPerDimensionWorldBorderPacket(dimension -> new WorldBorderInterpolateSizeS2CPacket(dimension.getWorldBorder()));
        sendPerDimensionWorldBorderPacket(dimension -> new WorldBorderWarningBlocksChangedS2CPacket(dimension.getWorldBorder()));
        sendPerDimensionWorldBorderPacket(dimension -> new WorldBorderWarningTimeChangedS2CPacket(dimension.getWorldBorder()));
    }

    public static void sendPerDimensionWorldBorderPacket(Function<ServerWorld, Packet<?>> packetProvider) {
        // NOTE: I don't know which dimension is changed, so I just simply update the world border for all dimensions.
        ServerHelper
            .getWorlds()
            .forEach(dimension -> {
                WorldBorder perDimensionWorldBorder = dimension.getWorldBorder();
                RegistryKey<World> dimensionRegistryKey = dimension.getRegistryKey();
                Packet<?> packet = packetProvider.apply(dimension);

                ServerHelper.getPlayerManager().sendToDimension(packet, dimensionRegistryKey);
            });
    }
}

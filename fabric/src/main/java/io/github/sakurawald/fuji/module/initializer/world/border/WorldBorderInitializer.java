package io.github.sakurawald.fuji.module.initializer.world.border;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.WorldHelper;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.document.annotation.TestCase;
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
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;

@Document(id = 1752561532728L, value = """
    This module allows you to customize the `per-dimension border`.
    """)
@ColorBox(id = 1753064857726L, color = ColorBox.ColorBoxTypes.NOTE, value = """
    ◉ How it works?
    In internal Minecraft, each `dimension` has a function named `getWorldBorder()`.
    It returns the `world border` of this `dimension`.

    The vanilla Minecraft only returns the `world border` of `minecraft:overworld`.
    Fuji modify the `getWorldBorder()` function, to let it return the `per-dimension border`.

    ◉ How can I configure the `per-dimension border`?
    You can modify the config file directly, and issue `/fuji reload` to apply changes.

    ◉ Can I use this module in vanilla dimensions?
    Yes, you can.
    """)
@ColorBox(id = 1752460350802L, color = ColorBox.ColorBoxTypes.TIPS, value = """
    ◉ The logic of `/worldborder` command.
    The `/worldborder` command only sets the `World Border` of `minecraft:overworld`.
    But the `minecraft:the_nether` and `minecraft:the_end` dimensions will `sync` the `world border` of `minecraft:overworld`.
    """)
@ColorBox(id = 1752569349615L, color = ColorBox.ColorBoxTypes.TIPS, value = """
    ◉ The semantics of options for `world border`.
    See: https://minecraft.wiki/w/World_border
    """)
@TestCase(action = "Issue `/tp` and `/world tp` between dimensions.", targets = "The per-dimension border should be synced on the client-side.")
public class WorldBorderInitializer extends ModuleInitializer {

    private static final BaseConfigurationHandler<WorldBorderConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON_LITERAL, WorldBorderConfigModel.class);

    public static Optional<BorderDescriptor> getEffectiveBorderDescriptor(String dimensionId) {
        return config.model().borders
            .stream()
            .filter(it -> it.enable
                && it.dimensionId.equals(dimensionId))
            .findFirst();
    }

    @Override
    protected void onReload() {
        sendWorldBorderSyncPacketsToAllPlayers();
    }

    private static void sendWorldBorderSyncPacketsToAllPlayers() {
        sendPerDimensionPacketToAllDimensions(dimension -> new WorldBorderCenterChangedS2CPacket(dimension.getWorldBorder()));
        sendPerDimensionPacketToAllDimensions(dimension -> new WorldBorderSizeChangedS2CPacket(dimension.getWorldBorder()));
        sendPerDimensionPacketToAllDimensions(dimension -> new WorldBorderInterpolateSizeS2CPacket(dimension.getWorldBorder()));
        sendPerDimensionPacketToAllDimensions(dimension -> new WorldBorderWarningBlocksChangedS2CPacket(dimension.getWorldBorder()));
        sendPerDimensionPacketToAllDimensions(dimension -> new WorldBorderWarningTimeChangedS2CPacket(dimension.getWorldBorder()));
    }

    public static void sendWorldBorderSyncPacketsToPlayer(ServerPlayerEntity player, World world) {
        player.networkHandler.sendPacket(new WorldBorderCenterChangedS2CPacket(world.getWorldBorder()));
        player.networkHandler.sendPacket(new WorldBorderSizeChangedS2CPacket(world.getWorldBorder()));
        player.networkHandler.sendPacket(new WorldBorderInterpolateSizeS2CPacket(world.getWorldBorder()));
        player.networkHandler.sendPacket(new WorldBorderWarningBlocksChangedS2CPacket(world.getWorldBorder()));
        player.networkHandler.sendPacket(new WorldBorderWarningTimeChangedS2CPacket(world.getWorldBorder()));
    }

    public static void sendPerDimensionPacketToAllDimensions(Function<ServerWorld, Packet<?>> packetProvider) {
        // NOTE: I don't know which dimension is changed, so I just simply update the world border for all dimensions.
        WorldHelper
            .getWorlds()
            .forEach(dimension -> {
                @SuppressWarnings("unused")
                WorldBorder callGetterMethodToUpdateEffectiveDescriptor = dimension.getWorldBorder();
                RegistryKey<World> dimensionRegistryKey = dimension.getRegistryKey();
                Packet<?> packet = packetProvider.apply(dimension);

                PlayerHelper.getPlayerManager().sendToDimension(packet, dimensionRegistryKey);
            });
    }
}

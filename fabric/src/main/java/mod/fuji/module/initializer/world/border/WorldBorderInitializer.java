package mod.fuji.module.initializer.world.border;

import mod.fuji.core.annotation.HotPath;
import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.auxiliary.minecraft.WorldHelper;
import mod.fuji.core.config.handler.abst.BaseConfigurationHandler;
import mod.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import mod.fuji.core.document.annotation.ColorBox;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.document.annotation.TestCase;
import mod.fuji.module.initializer.ModuleInitializer;
import mod.fuji.module.initializer.world.border.config.model.WorldBorderConfigModel;
import mod.fuji.module.initializer.world.border.structure.BorderDescriptor;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetBorderCenterPacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderLerpSizePacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderSizePacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderWarningDistancePacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderWarningDelayPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.border.WorldBorder;

@Document(id = 1752561532728L, value = """
    This module allows you to customize the `per-dimension border`.
    """)
@ColorBox(id = 1758445255938L, color = ColorBox.ColorBoxTypes.WARNING, value = """
    ◉ The `per-dimension worldborder` since `MC 1.21.9`
    Since `MC 1.21.9`, the `/worldborder` command is `dimension specific`.
    You should only use `world.border` module when your MC version is lower than `MC 1.21.9`.
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
@ColorBox(id = 1752460350802L, color = ColorBox.ColorBoxTypes.TIP, value = """
    ◉ The logic of `/worldborder` command.
    The `/worldborder` command only sets the `World Border` of `minecraft:overworld`.
    But the `minecraft:the_nether` and `minecraft:the_end` dimensions will `sync` the `world border` of `minecraft:overworld`.
    """)
@ColorBox(id = 1752569349615L, color = ColorBox.ColorBoxTypes.TIP, value = """
    ◉ The semantics of options for `world border`.
    See: https://minecraft.wiki/w/World_border
    """)
@TestCase(action = "Issue `/tp` and `/world tp` between dimensions.", targets = "The per-dimension border should be synced on the client-side.")
public class WorldBorderInitializer extends ModuleInitializer {

    private static final BaseConfigurationHandler<WorldBorderConfigModel> config = ObjectConfigurationHandler.ofModule(BaseConfigurationHandler.CONFIG_JSON_LITERAL, WorldBorderConfigModel.class);

    @HotPath("Many game logics will check the game border.")
    public static Optional<BorderDescriptor> getEffectiveBorderDescriptor(String dimensionId) {
        for (BorderDescriptor borderDescriptor : config.model().borders) {
            if (borderDescriptor.enable && borderDescriptor.dimensionId.equals(dimensionId)) {
                return Optional.of(borderDescriptor);
            }
        }

        return Optional.empty();
    }

    @Override
    protected void onReload() {
        sendWorldBorderSyncPacketsToAllPlayers();
    }

    private static void sendWorldBorderSyncPacketsToAllPlayers() {
        sendPerDimensionPacketToAllDimensions(dimension -> new ClientboundSetBorderCenterPacket(dimension.getWorldBorder()));
        sendPerDimensionPacketToAllDimensions(dimension -> new ClientboundSetBorderSizePacket(dimension.getWorldBorder()));
        sendPerDimensionPacketToAllDimensions(dimension -> new ClientboundSetBorderLerpSizePacket(dimension.getWorldBorder()));
        sendPerDimensionPacketToAllDimensions(dimension -> new ClientboundSetBorderWarningDistancePacket(dimension.getWorldBorder()));
        sendPerDimensionPacketToAllDimensions(dimension -> new ClientboundSetBorderWarningDelayPacket(dimension.getWorldBorder()));
    }

    public static void sendWorldBorderSyncPacketsToPlayer(ServerPlayer player, Level world) {
        player.connection.send(new ClientboundSetBorderCenterPacket(world.getWorldBorder()));
        player.connection.send(new ClientboundSetBorderSizePacket(world.getWorldBorder()));
        player.connection.send(new ClientboundSetBorderLerpSizePacket(world.getWorldBorder()));
        player.connection.send(new ClientboundSetBorderWarningDistancePacket(world.getWorldBorder()));
        player.connection.send(new ClientboundSetBorderWarningDelayPacket(world.getWorldBorder()));
    }

    public static void sendPerDimensionPacketToAllDimensions(Function<ServerLevel, Packet<?>> packetProvider) {
        // NOTE: I don't know which dimension is changed, so I just simply update the world border for all dimensions.
        WorldHelper
            .getWorlds()
            .forEach(dimension -> {
                @SuppressWarnings("unused")
                WorldBorder callGetterMethodToUpdateEffectiveDescriptor = dimension.getWorldBorder();
                ResourceKey<Level> dimensionRegistryKey = dimension.dimension();
                Packet<?> packet = packetProvider.apply(dimension);

                PlayerHelper.getPlayerManager().broadcastAll(packet, dimensionRegistryKey);
            });
    }
}

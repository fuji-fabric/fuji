package io.github.sakurawald.fuji.module.initializer.command_toolbox.tppos;

import io.github.sakurawald.fuji.core.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.EntityHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.RegistryHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.WorldHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.command.annotation.CommandTarget;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.Dimension;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.OfflinePlayerName;
import io.github.sakurawald.fuji.core.service.random_teleport.RandomTeleporter;
import io.github.sakurawald.fuji.core.structure.GlobalPos;
import io.github.sakurawald.fuji.core.structure.TeleportSetup;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import java.util.Optional;


@Document("""
    Provides `/tppos` command.
    A unified teleport command.
    """)
public class TpposInitializer extends ModuleInitializer {

    @Document("The unified teleport command.")
    @CommandNode("tppos")
    @CommandRequirement(level = 4)
    private static int tppos(@CommandSource @CommandTarget ServerPlayerEntity player
        , @Document("the target dimension") Optional<Dimension> dimension
        , @Document("the target x for fixed-tp") Optional<Double> x
        , @Document("the target y for fixed-tp") Optional<Double> y
        , @Document("the target z for fixed-tp") Optional<Double> z
        , @Document("the target yaw for fixed-tp") Optional<Float> yaw
        , @Document("the target pitch for fixed-tp") Optional<Float> pitch
        , @Document("center x for rtp") Optional<Integer> centerX
        , @Document("center z for rtp") Optional<Integer> centerZ
        , @Document("is the shape of rtp circle or square") Optional<Boolean> circle
        , @Document("min radius for rtp") Optional<Integer> minRange
        , @Document("max radius for rtp") Optional<Integer> maxRange
        , @Document("min y for rtp") Optional<Integer> minY
        , @Document("max y for rtp") Optional<Integer> maxY
        , @Document("max try times for rtp") Optional<Integer> maxTryTimes
    ) {
        /* specify the dimension */
        ServerWorld world = dimension.isPresent() ? dimension.get().getValue() : EntityHelper.getServerWorld(player);

        /* mode: fixed teleport */
        if (x.isPresent() || y.isPresent() || z.isPresent()) {
            double $x = x.orElse(player.getX());
            double $y = y.orElse(player.getY());
            double $z = z.orElse(player.getZ());
            float $yaw = yaw.orElse(player.getYaw());
            float $pitch = pitch.orElse(player.getPitch());


            GlobalPos globalPos = new GlobalPos(world, $x, $y, $z, $yaw, $pitch);
            globalPos.teleport(player);
            return CommandHelper.Return.SUCCESS;
        }

        /* mode: random teleport */
        int $centerX = centerX.orElse((int) world.getWorldBorder().getCenterX());
        int $centerZ = centerZ.orElse((int) world.getWorldBorder().getCenterZ());
        boolean $circle = circle.orElse(false);
        int $minRange = minRange.orElse(0);
        int $maxRange = maxRange.orElse((int) world.getWorldBorder().getSize() / 2);
        int $minY = minY.orElse(world.getBottomY());
        int $maxY = maxY.orElse(WorldHelper.getTopY(world));
        int $maxTryTimes = maxTryTimes.orElse(8);

        TeleportSetup teleportSetup = new TeleportSetup(RegistryHelper.ofString(world), $centerX, $centerZ, $circle, $minRange, $maxRange, $minY
            , $maxY, $maxTryTimes);

        RandomTeleporter.request(player, teleportSetup, null);
        return CommandHelper.Return.SUCCESS;
    }

    @Document("Teleport to the offline position of a player.")
    @CommandNode("tppos offline")
    @CommandRequirement(level = 4)
    private static int tppos(@CommandSource ServerPlayerEntity source, OfflinePlayerName player) {
        ServerPlayerEntity dummy = PlayerHelper.loadOfflinePlayer(player.getValue());
        new GlobalPos(EntityHelper.getServerWorld(dummy), dummy.getX(), dummy.getY(), dummy.getZ(), dummy.getYaw(), dummy.getPitch())
            .teleport(source);
        return CommandHelper.Return.SUCCESS;
    }

}

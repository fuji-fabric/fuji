package io.github.sakurawald.module.initializer.command_toolbox.tppos;

import io.github.sakurawald.core.annotation.Document;
import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.core.auxiliary.minecraft.EntityHelper;
import io.github.sakurawald.core.auxiliary.minecraft.RegistryHelper;
import io.github.sakurawald.core.command.annotation.CommandNode;
import io.github.sakurawald.core.command.annotation.CommandRequirement;
import io.github.sakurawald.core.command.annotation.CommandSource;
import io.github.sakurawald.core.command.annotation.CommandTarget;
import io.github.sakurawald.core.command.argument.wrapper.impl.Dimension;
import io.github.sakurawald.core.command.argument.wrapper.impl.OfflinePlayerName;
import io.github.sakurawald.core.service.random_teleport.RandomTeleporter;
import io.github.sakurawald.core.structure.SpatialPose;
import io.github.sakurawald.core.structure.TeleportSetup;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import java.util.Optional;


public class TpposInitializer extends ModuleInitializer {

    @CommandNode("tppos")
    @CommandRequirement(level = 4)
    @Document("The unified teleport command.")
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
        ServerWorld world = dimension.isPresent() ? dimension.get().getValue() : player.getServerWorld();

        /* mode: fixed teleport */
        if (x.isPresent() || y.isPresent() || z.isPresent()) {
            double $x = x.orElse(player.getX());
            double $y = y.orElse(player.getY());
            double $z = z.orElse(player.getZ());
            float $yaw = yaw.orElse(player.getYaw());
            float $pitch = pitch.orElse(player.getPitch());


            SpatialPose spatialPose = new SpatialPose(world, $x, $y, $z, $yaw, $pitch);
            spatialPose.teleport(player);
            return CommandHelper.Return.SUCCESS;
        }

        /* mode: random teleport */
        int $centerX = centerX.orElse((int) world.getWorldBorder().getCenterX());
        int $centerZ = centerZ.orElse((int) world.getWorldBorder().getCenterZ());
        boolean $circle = circle.orElse(false);
        int $minRange = minRange.orElse(0);
        int $maxRange = maxRange.orElse((int) world.getWorldBorder().getSize() / 2);
        int $minY = minY.orElse(world.getBottomY());
        int $maxY = maxY.orElse(world.getTopYInclusive());
        int $maxTryTimes = maxTryTimes.orElse(8);

        TeleportSetup teleportSetup = new TeleportSetup(RegistryHelper.ofString(world), $centerX, $centerZ, $circle, $minRange, $maxRange, $minY
            , $maxY, $maxTryTimes);

        RandomTeleporter.request(player, teleportSetup, null);
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("tppos offline")
    @CommandRequirement(level = 4)
    @Document("Teleport to the offline position of a player.")
    private static int tppos(@CommandSource ServerPlayerEntity source, OfflinePlayerName player) {
        ServerPlayerEntity dummy = EntityHelper.loadOfflinePlayer(player.getValue());
        new SpatialPose(dummy.getServerWorld(), dummy.getX(), dummy.getY(), dummy.getZ(), dummy.getYaw(), dummy.getPitch()).teleport(source);
        return CommandHelper.Return.SUCCESS;
    }

}

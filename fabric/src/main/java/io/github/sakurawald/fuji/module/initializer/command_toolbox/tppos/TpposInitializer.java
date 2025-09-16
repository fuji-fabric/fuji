package io.github.sakurawald.fuji.module.initializer.command_toolbox.tppos;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.EntityHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.RegistryHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.WorldHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.command.annotation.CommandTarget;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.BiomeId;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.Dimension;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.OfflinePlayerName;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.PlayerCollection;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.document.annotation.TestCase;
import io.github.sakurawald.fuji.core.service.random_teleport.RandomTeleporter;
import io.github.sakurawald.fuji.core.service.random_teleport.structure.RandomTeleportSettings;
import io.github.sakurawald.fuji.core.structure.GlobalPos;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;


@Document(id = 1751825242047L, value = """
    Provides `/tppos` command.
    A `unified` and `powerful` teleport command.

    For example:
    1. `/tppos --z 64 --x 32 --y 128` to teleport to `a specified position`
    2. `/tppos --others Steve` to specify the `target player`.
    3. `/tppos offline Alex` to teleport to the `offline position of Alex`.
    4. `/tppos --dimension` to specify the `target dimension`, and start `a random tp`.
    5. `/tppos --minRange 1000 --maxRange 2000` to specify the setup for `a random tp`.
    6. `/tppos here @a` to teleport `all online players` to `you`.
    """)
@TestCase(action = "Issue the command `/tppos --z 64 --x 32 --y 128`", targets = "The command context should be passed after the command redirection.")
public class TpposInitializer extends ModuleInitializer {

    @SuppressWarnings("SameReturnValue")
    @Document(id = 1751825250986L, value = "The unified teleport command.")
    @CommandNode("tppos")
    @CommandRequirement(level = 4)
    private static int $tppos(@CommandSource @CommandTarget ServerPlayerEntity player
        , @Document(id = 1751825286136L, value = "the target dimension") Optional<Dimension> dimension
        , @Document(id = 1751825291728L, value = "the target x for fixed-tp") Optional<Double> x
        , @Document(id = 1751825295183L, value = "the target y for fixed-tp") Optional<Double> y
        , @Document(id = 1751825298686L, value = "the target z for fixed-tp") Optional<Double> z
        , @Document(id = 1751825302162L, value = "the target yaw for fixed-tp") Optional<Float> yaw
        , @Document(id = 1751825308511L, value = "the target pitch for fixed-tp") Optional<Float> pitch
        , @Document(id = 1751825312187L, value = "center x for rtp") Optional<Integer> centerX
        , @Document(id = 1751825318126L, value = "center z for rtp") Optional<Integer> centerZ
        , @Document(id = 1751825322575L, value = "is the shape of rtp circle or square") Optional<Boolean> circle
        , @Document(id = 1751825327440L, value = "min radius for rtp") Optional<Integer> minRange
        , @Document(id = 1751825331634L, value = "max radius for rtp") Optional<Integer> maxRange
        , @Document(id = 1751825335795L, value = "min y for rtp") Optional<Integer> minY
        , @Document(id = 1751825340303L, value = "max y for rtp") Optional<Integer> maxY
        , @Document(id = 1751825344683L, value = "max try times for rtp") Optional<Integer> maxTryTimes
        , Optional<Integer> asyncChunkLoadingTimeoutTicks
        , Optional<Integer> chunkInhabitedTimeLowerThanTicks
        , Optional<BiomeId> biome
    ) {
        /* Specify the dimension */
        ServerWorld world = dimension.isPresent() ? dimension.get().getValue() : EntityHelper.getServerWorld(player);

        /* Mode: fixed teleport */
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

        /* Mode: random teleport */
        int $centerX = centerX.orElse((int) world.getWorldBorder().getCenterX());
        int $centerZ = centerZ.orElse((int) world.getWorldBorder().getCenterZ());
        boolean $circle = circle.orElse(false);
        int $minRange = minRange.orElse(0);
        int $maxRange = maxRange.orElse((int) world.getWorldBorder().getSize() / 2);
        int $minY = minY.orElse(world.getBottomY());
        int $maxY = maxY.orElse(WorldHelper.getTopY(world));
        int $maxTryTimes = maxTryTimes.orElse(8);
        String worldId = RegistryHelper.getIdAsString(world);
        int $asyncChunkLoadingTimeoutTicks = asyncChunkLoadingTimeoutTicks.orElse(20 * 10);
        int $chunkInhabitedTimeLowerThanTicks = chunkInhabitedTimeLowerThanTicks.orElse(Integer.MAX_VALUE);
        RandomTeleportSettings.Biomes biomes = biome
            .map($targetBiome -> {
                RandomTeleportSettings.Biomes result = new RandomTeleportSettings.Biomes();

                /* Clear skipped biomes. */
                result.getSkip().clear();

                /* Enable biome whitelist mode. */
                result.getOnlyAcceptBiomesMode().setEnable(true);
                Identifier biomeId = $targetBiome.getValue();
                result.getOnlyAcceptBiomesMode().setAccept(Set.of(RegistryHelper.getIdAsString(biomeId)));
                return result;
            })
            .orElseGet(RandomTeleportSettings.Biomes::new);

        RandomTeleportSettings randomTeleportSettings = new RandomTeleportSettings(true, worldId, $centerX, $centerZ, $circle, $minRange, $maxRange, $minY, $maxY, $maxTryTimes, $asyncChunkLoadingTimeoutTicks, $chunkInhabitedTimeLowerThanTicks, biomes, new RandomTeleportSettings.Blocks());

        RandomTeleporter.request(player, randomTeleportSettings, null);
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751825355777L, value = "Teleport to the offline position of a player.")
    @CommandNode("tppos offline")
    @CommandRequirement(level = 4)
    @TestCase(action = "Teleport to an offline player's location using `/tppos offline`", targets = {
        "We should be able to make the offline player instance."
        , "The saved dimension of the offline player should not be reset to minecraft:overworld"
    })
    private static int $tppos(@CommandSource ServerPlayerEntity source, OfflinePlayerName player) {
        ServerPlayerEntity dummyPlayer = PlayerHelper.Loader.loadDummyPlayer(player.getValue());
        GlobalPos
            .of(dummyPlayer)
            .teleport(source);
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751825360963L, value = """
        Teleport `others` to `you`.

        For example:
        1. `/tppos here Steve` to teleport `Steve` to `you`.
        2. `/tppos here @a` to teleport `all online players` to `you`.
        """)
    @CommandNode("tppos here")
    @CommandRequirement(level = 4)
    private static int $tppos(@CommandSource ServerPlayerEntity source, PlayerCollection targets) {
        Collection<ServerPlayerEntity> $targets = targets.getValue();
        GlobalPos globalPos = GlobalPos.of(source);
        $targets.forEach(globalPos::teleport);
        return CommandHelper.Return.SUCCESS;
    }

}

package io.github.sakurawald.fuji.core.service.random_teleport;

import com.google.common.base.Stopwatch;
import io.github.sakurawald.fuji.core.auxiliary.AsyncUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.RegistryHelper;
import io.github.sakurawald.fuji.core.document.annotation.Cite;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.WorldHelper;
import io.github.sakurawald.fuji.core.structure.GlobalPos;
import io.github.sakurawald.fuji.core.service.random_teleport.structure.RandomTeleportSettings;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.function.Consumer;
import java.util.Optional;

@Cite("https://github.com/John-Paul-R/Essential-Commands")
public class RandomTeleporter {

    public static void request(@NotNull ServerPlayerEntity player, @NotNull RandomTeleportSettings settings, @Nullable Consumer<GlobalPos> onCompleteHook) {
        AsyncUtil.runAsyncAndHandleExceptions(() -> {
            /* Start the timer. */
            String playerName = PlayerHelper.getPlayerName(player);
            LogUtil.info("Request rtp: {}", playerName);
            Stopwatch timer = Stopwatch.createStarted();

            /* Initialize world variable. */
            Optional<ServerWorld> world = WorldHelper.getWorld(settings.getDimension());
            if (world.isEmpty()) {
                LogUtil.warn("Abort rtp for {} (Target dimension not found in server)", player);
                TextHelper.sendTextByKey(player, "world.dimension.not_found");
                return;
            }
            ServerWorld $world = world.get();

            /* Do search. */
            final LocationSearchContext context = LocationSearchContext.of(settings);
            do {
                TextHelper.sendTextByKey(player, "rtp.progress.searching", context.getAttempts(), context.getMaxAttempts());
                context.incrementAttempts();
                PositionSearcher.search(context);
            } while (context.getResult().isEmpty() && context.hasRemainingAttempts());

            Optional<BlockPos> result = context.getResult();
            if (result.isEmpty()) {
                LogUtil.debug("Abort rtp for {}, run out attempts.", player);
                TextHelper.sendTextByKey(player, "rtp.progress.run_out_attempts");
                return;
            }

            /* Consume the search result. */
            BlockPos $result = result.get();
            GlobalPos globalPos = new GlobalPos($world, $result.getX() + 0.5, $result.getY(), $result.getZ() + 0.5, 0, 0);
            ServerHelper.executeSync(() -> globalPos.teleport(player));

            /* Call hooks. */
            if (onCompleteHook != null) {
                onCompleteHook.accept(globalPos);
            }

            TextHelper.sendTextByKey(player, "rtp.progress.location_found");

            /* Stop the timer. */
            var cost = timer.stop();
            LogUtil.info("Response rtp: {} has been teleported to ({} {} {} {}) (cost = {})", playerName, RegistryHelper.getIdAsString($world), $result.getX(), $result.getY(), $result.getZ(), cost);
        });
    }

}

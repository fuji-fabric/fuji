package mod.fuji.core.service.random_teleport;

import com.google.common.base.Stopwatch;
import mod.fuji.core.auxiliary.AsyncUtil;
import mod.fuji.core.auxiliary.LogUtil;
import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.auxiliary.minecraft.RegistryHelper;
import mod.fuji.core.auxiliary.minecraft.ServerHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.auxiliary.minecraft.WorldHelper;
import mod.fuji.core.document.annotation.Cite;
import mod.fuji.core.service.random_teleport.searcher.PositionSearcher;
import mod.fuji.core.service.random_teleport.structure.PositionSearchContext;
import mod.fuji.core.service.random_teleport.structure.RandomTeleportSettings;
import mod.fuji.core.structure.GlobalPos;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Cite("https://github.com/John-Paul-R/Essential-Commands")
public class RandomTeleporter {

    public static void request(@NotNull ServerPlayerEntity player, @NotNull RandomTeleportSettings settings, @Nullable Consumer<GlobalPos> onCompleteHook) {
        AsyncUtil.runAsyncAndHandleExceptions(() -> {
            /* Start the timer. */
            String playerName = PlayerHelper.getPlayerName(player);
            Stopwatch timer = Stopwatch.createStarted();
            LogUtil.info("Request rtp: {}", playerName);
            TextHelper.sendTextByKey(player, "rtp.progress.started");

            /* Initialize world variable. */
            Optional<ServerWorld> world = WorldHelper.getWorld(settings.getDimension());
            if (world.isEmpty()) {
                LogUtil.warn("Abort rtp for {} (Target dimension not found in server)", player);
                TextHelper.sendTextByKey(player, "world.dimension.not_found");
                return;
            }
            ServerWorld $world = world.get();

            /* Do search. */
            final PositionSearchContext context = PositionSearchContext.of(player, settings);
            do {
                context.incrementAttempts();
                TextHelper.sendTextByKey(player, "rtp.progress.searching", context.getAttempts(), context.getMaxAttempts());

                PositionSearcher.search(context);

                if (player.isRemoved()) {
                    LogUtil.info("Abort RTP: The player {} has been removed.", playerName);
                    return;
                }
            } while (context.getResult().isEmpty() && context.hasRemainingAttempts());

            Optional<BlockPos> result = context.getResult();
            if (result.isEmpty()) {
                TextHelper.sendTextByKey(player, "rtp.progress.run_out_attempts");
                return;
            }

            /* Consume the search result. */
            BlockPos $result = result.get();
            TextHelper.sendTextByKey(player, "rtp.progress.location_found", $result.getX(), $result.getY(), $result.getZ());

            GlobalPos globalPos = new GlobalPos($world, $result.getX() + 0.5, $result.getY(), $result.getZ() + 0.5, 0, 0);
            TextHelper.sendTextByKey(player, "rtp.progress.teleporting");

            ServerHelper.executeSync(() -> globalPos.teleport(player));

            /* Call hooks. */
            if (onCompleteHook != null) {
                onCompleteHook.accept(globalPos);
            }

            /* Stop the timer. */
            var cost = timer.stop();
            LogUtil.info("Response rtp: {} has been teleported to ({} {} {} {}) (cost = {})", playerName, RegistryHelper.getIdAsString($world), $result.getX(), $result.getY(), $result.getZ(), cost);
        });
    }

}

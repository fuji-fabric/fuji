package mod.fuji.module.initializer.top_chunks.service;

import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.auxiliary.minecraft.WorldHelper;
import mod.fuji.module.initializer.top_chunks.TopChunksInitializer;
import mod.fuji.module.initializer.top_chunks.structure.ChunkScore;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.NotNull;

public class TopChunksService {

    public static List<ChunkScore> trimChunkScoreList(List<ChunkScore> chunkScores) {
        int begin = 0;
        int end = Math.min(chunkScores.size(), getMaxDisplayChunkScore());
        return chunkScores.subList(begin, end);
    }

    public static @NotNull PriorityQueue<ChunkScore> computeChunkScores(CommandSourceStack source) {
        PriorityQueue<ChunkScore> PQ = new PriorityQueue<>();

        /* Enumerate worlds. */
        for (ServerLevel world : WorldHelper.getWorlds()) {
            Map<ChunkPos, ChunkScore> topChunkReport = new HashMap<>();

            /* Enumerate entities in this world. */
            for (Entity entity : WorldHelper.getEntities(world)) {
                ChunkPos pos = entity.chunkPosition();
                topChunkReport.putIfAbsent(pos, new ChunkScore(world, pos));
                topChunkReport.get(pos).plusEntity(entity);
            }

            /* Enumerate block entities in this world */
            for (ChunkHolder chunkHolder : WorldHelper.getChunks(world)) {
                LevelChunk worldChunk = chunkHolder.getTickingChunk();

                /* Check if the chunk is LOADED. */
                if (worldChunk == null) continue;

                /* Enumerate block entities in the chunk. */
                for (BlockEntity blockEntity : worldChunk.getBlockEntities().values()) {
                    ChunkPos pos = worldChunk.getPos();
                    topChunkReport.putIfAbsent(pos, new ChunkScore(world, pos));
                    topChunkReport.get(pos).plusBlockEntity(blockEntity);
                }
            }

            /* Sort chunk scores in PQ. */
            topChunkReport.values().forEach(chunkScore -> {
                chunkScore.sum();
                PQ.add(chunkScore);
            });
        }

        /* Attach nearest player into chunks. */
        attachNearestPlayerIntoChunkScore(source, PQ, getMaxDisplayChunkScore());
        return PQ;
    }

    private static void attachNearestPlayerIntoChunkScore(CommandSourceStack source, @NotNull PriorityQueue<ChunkScore> PQ, int topN) {
        int count = 0;
        for (ChunkScore chunkScore : PQ) {
            if (count++ >= topN) break;

            Level world = chunkScore.getDimension();
            ChunkPos chunkPos = chunkScore.getChunkPos();
            BlockPos blockPos = chunkPos.getWorldPosition();
            Player nearestPlayer = world.getNearestPlayer(blockPos.getX(), blockPos.getY(), blockPos.getZ(), TopChunksInitializer.config.model().distanceToSearchNearestPlayer, false);

            if (nearestPlayer != null) {
                String nearestPlayerName = PlayerHelper.getPlayerName(nearestPlayer);
                String nearestPlayerString = TextHelper.Operators.getString(TextHelper.getTextByKey(source, "top_chunks.prop.players.nearest", nearestPlayerName));
                chunkScore.getPlayers().add(nearestPlayerString);
            }
        }
    }

    private static int getMaxDisplayChunkScore() {
        var top = TopChunksInitializer.config.model().top;
        return top.rows * top.columns;
    }
}

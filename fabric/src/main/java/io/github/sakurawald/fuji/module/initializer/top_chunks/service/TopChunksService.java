package io.github.sakurawald.fuji.module.initializer.top_chunks.service;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.WorldHelper;
import io.github.sakurawald.fuji.module.initializer.top_chunks.TopChunksInitializer;
import io.github.sakurawald.fuji.module.initializer.top_chunks.structure.ChunkScore;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.NotNull;

public class TopChunksService {

    public static List<ChunkScore> trimChunkScoreList(List<ChunkScore> chunkScores) {
        int begin = 0;
        int end = Math.min(chunkScores.size(), getMaxDisplayChunkScore());
        return chunkScores.subList(begin, end);
    }

    public static @NotNull PriorityQueue<ChunkScore> computeChunkScores(ServerCommandSource source) {
        PriorityQueue<ChunkScore> PQ = new PriorityQueue<>();

        /* Enumerate worlds. */
        for (ServerWorld world : WorldHelper.getWorlds()) {
            Map<ChunkPos, ChunkScore> topChunkReport = new HashMap<>();

            /* Enumerate entities in this world. */
            for (Entity entity : world.iterateEntities()) {
                ChunkPos pos = entity.getChunkPos();
                topChunkReport.putIfAbsent(pos, new ChunkScore(world, pos));
                topChunkReport.get(pos).plusEntity(entity);
            }

            /* Enumerate block entities in this world */
            for (ChunkHolder chunkHolder : WorldHelper.getChunks(world)) {
                WorldChunk worldChunk = chunkHolder.getWorldChunk();

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

    private static void attachNearestPlayerIntoChunkScore(ServerCommandSource source, @NotNull PriorityQueue<ChunkScore> PQ, int topN) {
        int count = 0;
        for (ChunkScore chunkScore : PQ) {
            if (count++ >= topN) break;

            World world = chunkScore.getDimension();
            ChunkPos chunkPos = chunkScore.getChunkPos();
            BlockPos blockPos = chunkPos.getStartPos();
            PlayerEntity nearestPlayer = world.getClosestPlayer(blockPos.getX(), blockPos.getY(), blockPos.getZ(), TopChunksInitializer.config.model().nearest_distance, false);

            if (nearestPlayer != null) {
                String nearestPlayerName = PlayerHelper.getPlayerName(nearestPlayer);
                String nearestPlayerString = TextHelper.Operators.visitString(TextHelper.getTextByKey(source, "top_chunks.prop.players.nearest", nearestPlayerName));
                chunkScore.getPlayers().add(nearestPlayerString);
            }
        }
    }

    private static int getMaxDisplayChunkScore() {
        var top = TopChunksInitializer.config.model().top;
        return top.rows * top.columns;
    }
}

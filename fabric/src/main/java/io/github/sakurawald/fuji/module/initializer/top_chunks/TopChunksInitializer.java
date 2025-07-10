package io.github.sakurawald.fuji.module.initializer.top_chunks;

import io.github.sakurawald.fuji.core.AsyncUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.top_chunks.config.model.TopChunksConfigModel;
import io.github.sakurawald.fuji.module.initializer.top_chunks.gui.TopChunksGui;
import io.github.sakurawald.fuji.module.initializer.top_chunks.structure.ChunkScore;
import java.util.List;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;


@Document(id = 1751826535209L, value = """
    Analyze all loaded chunks of the server, and find the most lagged chunks.
    """)
@ColorBox(id = 1751981000562L, color = ColorBox.ColorBlockTypes.NOTE, value = """
    ◉ How it works?
    This module use a `simple statistical method` to estimate the `degree of lag of a chunk`.
    The method is simple, it simply counts the `entities` and `block entities` inside a `chunk`.
    And sum up the `score` by the `type` of `entity` or `block entity`.

    It's simple, fast and useful.
    You can define the score of `a zombie` as `4`.
    The score of `a bee` as `15`.
    And the score of `a piston` as `10`.
    That depends on your case.
    Simple method often works.
    """)
public class TopChunksInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<TopChunksConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, TopChunksConfigModel.class);

    @Document(id = 1751826537195L, value = "List all chunks ordered by lag score.")
    @CommandNode("chunks")
    private static int $chunks(@CommandSource ServerCommandSource source) {
        AsyncUtil.runAsyncAndHandleExceptions(() -> {
            PriorityQueue<ChunkScore> PQ = new PriorityQueue<>();

            /* Enumerate worlds. */
            for (ServerWorld world : ServerHelper.getWorlds()) {
                Map<ChunkPos, ChunkScore> topChunkReport = new HashMap<>();

                /* Enumerate entities in this world. */
                for (Entity entity : world.iterateEntities()) {
                    ChunkPos pos = entity.getChunkPos();
                    topChunkReport.putIfAbsent(pos, new ChunkScore(world, pos));
                    topChunkReport.get(pos).plusEntity(entity);
                }

                /* Enumerate block entities in this world */
                for (ChunkHolder chunkHolder : ServerHelper.getChunks(world)) {
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

            /* Send top chunks report. */
            sendTopChunksReport(source, PQ);
        });

        return CommandHelper.Return.SUCCESS;
    }

    private static void sendTopChunksReport(ServerCommandSource source, PriorityQueue<ChunkScore> PQ) {
        if (source.isExecutedByPlayer()) {
            sendTopChunksReportAsGui(source, PQ);
        } else {
            sendTopChunksReportAsMessage(source, PQ);
        }
    }

    private static void sendTopChunksReportAsGui(ServerCommandSource source, PriorityQueue<ChunkScore> PQ) {
        new TopChunksGui(source.getPlayer(), PQ.stream().toList(), 0)
            .open();
    }

    private static void sendTopChunksReportAsMessage(ServerCommandSource source, PriorityQueue<ChunkScore> PQ) {
        var config = TopChunksInitializer.config.model();

        MutableText reportText = Text.empty();
        outer:
        for (int j = 0; j < config.top.rows; j++) {
            for (int i = 0; i < config.top.columns; i++) {
                if (PQ.isEmpty()) break outer;
                reportText
                    .append(PQ.poll().asText(source))
                    .append(TextHelper.TEXT_SPACE);
            }
            reportText.append(TextHelper.TEXT_NEWLINE);
        }
        source.sendMessage(reportText);
    }

    private static void attachNearestPlayerIntoChunkScore(ServerCommandSource source, @NotNull PriorityQueue<ChunkScore> PQ, int topN) {
        int count = 0;
        for (ChunkScore chunkScore : PQ) {
            if (count++ >= topN) break;

            World world = chunkScore.getDimension();
            ChunkPos chunkPos = chunkScore.getChunkPos();
            BlockPos blockPos = chunkPos.getStartPos();
            PlayerEntity nearestPlayer = world.getClosestPlayer(blockPos.getX(), blockPos.getY(), blockPos.getZ(), config.model().nearest_distance, false);

            if (nearestPlayer != null) {
                String nearestPlayerName = PlayerHelper.getPlayerName(nearestPlayer);
                String nearestPlayerString = TextHelper.Operators.visitString(TextHelper.getTextByKey(source, "top_chunks.prop.players.nearest", nearestPlayerName));
                chunkScore.getPlayers().add(nearestPlayerString);
            }
        }
    }

    public static List<ChunkScore> trimChunkScoreList(List<ChunkScore> chunkScores) {
        int begin = 0;
        int end = Math.min(chunkScores.size(), getMaxDisplayChunkScore());
        return chunkScores.subList(begin, end);
    }

    public static int getMaxDisplayChunkScore() {
        var top = config.model().top;
        return top.rows * top.columns;
    }
}

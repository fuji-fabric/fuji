package io.github.sakurawald.module.initializer.top_chunks.structure;

import io.github.sakurawald.core.auxiliary.minecraft.PermissionHelper;
import io.github.sakurawald.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.core.auxiliary.minecraft.RegistryHelper;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.structure.GlobalPos;
import io.github.sakurawald.core.structure.TypeFormatter;
import io.github.sakurawald.module.initializer.top_chunks.TopChunksInitializer;
import lombok.Data;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.Heightmap;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Data
public class ChunkScore implements Comparable<ChunkScore> {

    /* Chunk global position. */
    private final ServerWorld dimension;
    private final ChunkPos chunkPos;

    /* Chunk score. */
    private final Map<String, Integer> type2amount = new HashMap<>();
    private int score;

    /* Players in the chunk. */
    private final Set<String> players = new HashSet<>();

    public ChunkScore(ServerWorld dimension, ChunkPos chunkPos) {
        this.dimension = dimension;
        this.chunkPos = chunkPos;
    }

    public static boolean canClickToTeleportToThisChunk(ServerPlayerEntity player) {
        return player.hasPermissionLevel(4) || PermissionHelper.hasPermission(player.getUuid(), "top_chunks.teleport");
    }

    public void plusEntity(@NotNull Entity entity) {
        String type = entity.getType().getTranslationKey();

        type2amount.putIfAbsent(type, 0);
        type2amount.put(type, type2amount.get(type) + 1);

        if (entity instanceof ServerPlayerEntity player) {
            this.players.add(PlayerHelper.getName(player));
        }
    }

    public void plusBlockEntity(@NotNull BlockEntity blockEntity) {
        String type = blockEntity.getCachedState().getBlock().getTranslationKey();

        type2amount.putIfAbsent(type, 0);
        type2amount.put(type, type2amount.get(type) + 1);
    }

    public void sum() {
        this.score = 0;
        for (String type : this.type2amount.keySet()) {
            HashMap<String, Integer> type2score = TopChunksInitializer.config.model().type2score;
            this.score += type2score.getOrDefault(type, type2score.get("default")) * type2amount.get(type);
        }
    }

    @Override
    public @NotNull String toString() {
        return String.format("%-5d", this.score);
    }

    @Override
    public int compareTo(@NotNull ChunkScore that) {
        return Integer.compare(that.score, this.score);
    }

    public @NotNull Text asText(@NotNull ServerCommandSource source) {
        /* Make hover text. */
        MutableText hoverText = Text.empty()
            .formatted(Formatting.GOLD)
            .append(TextHelper.getTextByKey(source, "top_chunks.prop.dimension", RegistryHelper.ofString(this.dimension)))
            .append(TextHelper.TEXT_NEWLINE)
            .append(TextHelper.getTextByKey(source, "top_chunks.prop.chunk", computeChunkLocationString(source)))
            .append(TextHelper.TEXT_NEWLINE)
            .append(TextHelper.getTextByKey(source, "top_chunks.prop.score", this.score))
            .append(TextHelper.TEXT_NEWLINE)
            .append(TextHelper.getTextByKey(source, "top_chunks.prop.players", this.players))
            .append(TextHelper.TEXT_NEWLINE)
            .append(TypeFormatter.formatTypes(source, this.type2amount));

        /* Make chunk score text. */
        return Text.empty()
            .append(Text.literal(this.toString()))
            .fillStyle(Style.EMPTY
                .withHoverEvent(TextHelper.HoverEvent.makeShowTextAction(hoverText))
                .withFormatting(this.players.isEmpty() ? Formatting.GRAY : Formatting.DARK_GREEN)
            );
    }

    public String computeChunkLocationString(@NotNull ServerCommandSource source) {
        String chunkLocation;
        if (TopChunksInitializer.config.model().hide_location) {
            chunkLocation = TextHelper.getValueByKey(source, "top_chunks.prop.hidden");
            if (source.hasPermissionLevel(4)) {
                chunkLocation = TextHelper.getValueByKey(source, "top_chunks.prop.hidden.bypass", this.chunkPos.toString());
            }
        } else {
            chunkLocation = this.chunkPos.toString();
        }

        return chunkLocation;
    }

    public void teleportToThisChunk(ServerPlayerEntity player) {
        BlockPos chunkCenterPos = new BlockPos(chunkPos.getCenterX(), 128, chunkPos.getCenterZ());
        int y = dimension.getTopPosition(Heightmap.Type.MOTION_BLOCKING, chunkCenterPos).getY();
        // NOTE: If the chunk is unloaded, the map height will be -64
        if (y == -64) {
            y = 128;
        }
        new GlobalPos(dimension, chunkCenterPos.getX(), y, chunkCenterPos.getZ(), player.getYaw(), player.getPitch())
            .teleport(player);
    }
}

package mod.fuji.module.initializer.top_chunks.structure;

import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.LuckpermsHelper;
import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.auxiliary.minecraft.RegistryHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.document.annotation.DocStringProvider;
import mod.fuji.core.document.descriptor.PermissionDescriptor;
import mod.fuji.core.service.command_callback.CommandCallbackManager;
import mod.fuji.core.service.type_formatter.TypeFormatter;
import mod.fuji.core.structure.GlobalPos;
import mod.fuji.module.initializer.top_chunks.TopChunksInitializer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import lombok.Data;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Data
public class ChunkScore implements Comparable<ChunkScore> {

    @DocStringProvider(id = 1752000233472L, value = """
        Click to teleport to the chunk location.
        """)
    private static final PermissionDescriptor CLICK_TO_TELEPORT_TO_CHUNK_LOCATION_PERMISSION = new PermissionDescriptor("top_chunks.teleport", 1752000233472L);

    /* Chunk global position. */
    private final ServerLevel dimension;
    private final ChunkPos chunkPos;

    /* Chunk score. */
    private final Map<String, Integer> type2amount = new HashMap<>();
    private int score;

    /* Players in the chunk. */
    private final Set<String> players = new HashSet<>();

    public ChunkScore(ServerLevel dimension, ChunkPos chunkPos) {
        this.dimension = dimension;
        this.chunkPos = chunkPos;
    }

    public static boolean canClickToTeleportToThisChunk(@Nullable ServerPlayer player) {
        if (player == null) {
            return false;
        }
        return CommandHelper.Requirement.isOperator(player) || LuckpermsHelper.hasPermission(player.getUUID(), CLICK_TO_TELEPORT_TO_CHUNK_LOCATION_PERMISSION);
    }

    public void plusEntity(@NotNull Entity entity) {
        String type = entity.getType().getDescriptionId();

        type2amount.putIfAbsent(type, 0);
        type2amount.put(type, type2amount.get(type) + 1);

        if (entity instanceof ServerPlayer player) {
            this.players.add(PlayerHelper.getPlayerName(player));
        }
    }

    public void plusBlockEntity(@NotNull BlockEntity blockEntity) {
        String type = blockEntity.getBlockState().getBlock().getDescriptionId();

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

    public @NotNull Component toText(@NotNull CommandSourceStack source) {
        /* Make hover text. */
        MutableComponent hoverText = Component.empty()
            .withStyle(ChatFormatting.GOLD)
            .append(TextHelper.getTextByKey(source, "top_chunks.prop.dimension", RegistryHelper.getIdAsString(this.dimension)))
            .append(TextHelper.TEXT_NEWLINE)
            .append(this.computeChunkLocationText(source))
            .append(TextHelper.TEXT_NEWLINE)
            .append(TextHelper.getTextByKey(source, "top_chunks.prop.score", this.score))
            .append(TextHelper.TEXT_NEWLINE)
            .append(TextHelper.getTextByKey(source, "top_chunks.prop.players", this.players))
            .append(TextHelper.TEXT_NEWLINE)
            .append(TypeFormatter.formatTypes(source, this.type2amount));

        /* Make chunk score text style. */
        Style chunkScoreTextStyle = Style
            .EMPTY
            .withHoverEvent(TextHelper.Events.HoverEvent.makeShowTextAction(hoverText))
            .applyFormat(this.players.isEmpty() ? ChatFormatting.GRAY : ChatFormatting.DARK_GREEN);
        if (ChunkScore.canClickToTeleportToThisChunk(source.getPlayer())) {
            hoverText.append(TextHelper.TEXT_NEWLINE);
            hoverText.append(TextHelper.getTextByKey(source, "prompt.click.teleport"));

            ClickEvent clickEvent = CommandCallbackManager.makeCallbackClickEvent(this::teleportToThisChunk, 5, TimeUnit.MINUTES);
            chunkScoreTextStyle = chunkScoreTextStyle.withClickEvent(clickEvent);
        }

        /* Make chunk score text. */
        MutableComponent chunkScoreText = Component.empty()
            .append(Component.literal(this.toString()))
            .withStyle(chunkScoreTextStyle);
        return chunkScoreText;
    }

    public Component computeChunkLocationText(@NotNull CommandSourceStack source) {
        if (TopChunksInitializer.config.model().hide_location) {
            if (CommandHelper.Requirement.isAdmin(source)) {
                return TextHelper.getTextByKey(source, "top_chunks.prop.chunk.hide_location.bypass", this.chunkPos.toString());
            } else {
                return TextHelper.getTextByKey(source, "top_chunks.prop.chunk.hide_location");
            }

        } else {
            return TextHelper.getTextByKey(source, "top_chunks.prop.chunk.display_location", this.chunkPos.toString());
        }
    }

    public void teleportToThisChunk(ServerPlayer player) {
        BlockPos chunkCenterPos = new BlockPos(chunkPos.getMiddleBlockX(), 128, chunkPos.getMiddleBlockZ());
        int y = dimension.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, chunkCenterPos).getY();
        // NOTE: If the chunk is unloaded, the map height will be -64
        if (y == -64) {
            y = 128;
        }
        new GlobalPos(dimension, chunkCenterPos.getX(), y, chunkCenterPos.getZ(), player.getYRot(), player.getXRot())
            .teleport(player);
    }
}

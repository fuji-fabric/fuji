package io.github.sakurawald.fuji.module.initializer.rank.service;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.LuckpermsHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.document.annotation.DocStringProvider;
import io.github.sakurawald.fuji.core.document.descriptor.PermissionDescriptor;
import io.github.sakurawald.fuji.module.initializer.rank.RankInitializer;
import io.github.sakurawald.fuji.module.initializer.rank.structure.RankDataNode;
import io.github.sakurawald.fuji.module.initializer.rank.structure.RankNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RankService {

    @DocStringProvider(id = 1754414038512L, value = """
        The `starting rank node` is allowed to be selected for this `player`, if they have no rank yet.
        You can assign multiple `starting rank nodes` to be selected.
        """)
    private static final PermissionDescriptor RANK_STARTING_RANK_NODE_PERMISSION_DESCRIPTOR = new PermissionDescriptor("rank.starting_rank_node.<rank-node-id>", 1754414038512L);

    public static List<RankNode> getAllRankNodes() {
        return RankInitializer.config.model().getGraph()
            .stream()
            .filter(RankNode::isEnable)
            .toList();
    }

    public static List<String> getAllRankIds() {
        return getAllRankNodes()
            .stream()
            .map(RankNode::getId)
            .toList();
    }

    public static Optional<RankNode> findRankNode(@Nullable String id) {
        return getAllRankNodes()
            .stream()
            .filter(it -> it.getId().equals(id))
            .findFirst();
    }

    public static Optional<RankNode> getStartingRankNode() {
        String startingRankNodeId = RankInitializer.config.model().getStartingRankNodeId();
        return findRankNode(startingRankNodeId);
    }

    @SuppressWarnings("CollectionAddAllCanBeReplacedWithConstructor")
    public static List<RankNode> getAvailableStartingRankNodes(@NotNull ServerPlayerEntity player) {
        ArrayList<RankNode> result = new ArrayList<>();

        /* Get available starting rank nodes from permission. */
        List<RankNode> A = getAllRankNodes()
            .stream()
            .filter(it -> LuckpermsHelper.hasPermission(player.getUuid(), RANK_STARTING_RANK_NODE_PERMISSION_DESCRIPTOR, it.getId()))
            .toList();
        result.addAll(A);

        /* Get available starting rank node from the config. */
        getStartingRankNode()
            .ifPresent(result::add);

        /* Return the combined result. */
        return result;
    }

    public static void setCurrentRankNode(@NotNull ServerPlayerEntity player, @Nullable RankNode rankNode) {
        withRankDataNode(player, true, rankDataNode -> {
            String newValue = rankNode == null ? null : rankNode.getId();
            rankDataNode.setCurrentRankNodeId(newValue);
            return null;
        });
    }

    public static Optional<RankNode> getCurrentRankNode(@NotNull ServerPlayerEntity player) {
        return withRankDataNode(player, false, rankDataNode -> {
            @Nullable String currentRankNodeId = rankDataNode.getCurrentRankNodeId();
            return findRankNode(currentRankNodeId);
        });
    }

    private static <T> T withRankDataNode(@NotNull ServerPlayerEntity player, boolean writeStorage, Function<RankDataNode, T> function) {
        String playerName = PlayerHelper.getPlayerName(player);
        RankDataNode rankDataNode = RankInitializer.data.model()
            .getRankDataNodeMap()
            .computeIfAbsent(playerName, key -> RankDataNode.make());

        T apply = function.apply(rankDataNode);
        if (writeStorage) {
            RankInitializer.data.writeStorage();
        }
        return apply;
    }

    public static Text getNoRankStatusText() {
        return TextHelper.getTextByValue(null, RankInitializer.config.model().getNoRankStatusText());
    }

    public static List<RankNode> getNextAvailableRankNodes(@NotNull ServerPlayerEntity player) {
        return getCurrentRankNode(player)
            .map(RankService::getNextRankNodes)
            .orElseGet(() -> getAvailableStartingRankNodes(player));
    }

    private static @NotNull List<RankNode> getNextRankNodes(@NotNull RankNode it) {
        return it.getNextRankNodes()
            .stream()
            .map(id -> findRankNode(id).orElse(null))
            .filter(Objects::nonNull)
            .toList();
    }

    public static @NotNull List<String> getNextAvailableRankNodeIds(@NotNull ServerPlayerEntity player) {
        return getNextAvailableRankNodes(player)
            .stream()
            .map(RankNode::getId)
            .toList();
    }
}

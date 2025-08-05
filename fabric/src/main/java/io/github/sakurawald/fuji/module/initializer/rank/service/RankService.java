package io.github.sakurawald.fuji.module.initializer.rank.service;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.LuckpermsHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.executor.CommandExecutor;
import io.github.sakurawald.fuji.core.command.structure.ExtendedCommandSource;
import io.github.sakurawald.fuji.core.document.annotation.DocStringProvider;
import io.github.sakurawald.fuji.core.document.descriptor.PermissionDescriptor;
import io.github.sakurawald.fuji.module.initializer.rank.RankInitializer;
import io.github.sakurawald.fuji.module.initializer.rank.structure.RankDataNode;
import io.github.sakurawald.fuji.module.initializer.rank.structure.RankNode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
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

    private static final Map<RankNode, List<RankNode>> PREVIOUS_RANK_NODES_MAP = new HashMap<>();
    private static final Map<RankNode, List<RankNode>> NEXT_RANK_NODES_MAP = new HashMap<>();

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
        ExtendedCommandSource source = ExtendedCommandSource.asConsole(player.getCommandSource());

        /* Execute the leave commands for previous rank node. */
        Optional<RankNode> previousRankNode = getCurrentRankNode(player);
        previousRankNode
            .ifPresent(it -> {
                CommandExecutor.execute(source, it.getEvents().getOnLeaveThisRankNodeCommands());
            });

        /* Save the state. */
        withRankDataNode(player, true, rankDataNode -> {
            String newValue = rankNode == null ? null : rankNode.getId();
            rankDataNode.setCurrentRankNodeId(newValue);

            /* If new rank node is not null. */
            if (rankNode != null) {
                /* Execute the enter commands for new rank node. */
                CommandExecutor.execute(source, rankNode.getEvents().getOnEnterThisRankNodeCommands());

                /* Is the first time to enter this rank node? */
                if (!rankDataNode.getWalkedRankNodeIds().contains(newValue)) {
                    CommandExecutor.execute(source, rankNode.getEvents().getOnFirstEnterThisRankNodeCommands());
                }

                /* Remember this rank node. */
                rankDataNode.getWalkedRankNodeIds().add(rankNode.getId());
            }
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
            .map(NEXT_RANK_NODES_MAP::get)
            .orElseGet(() -> getAvailableStartingRankNodes(player));
    }

    public static List<RankNode> getPreviousAvailableRankNodes(@NotNull ServerPlayerEntity player) {
        return getCurrentRankNode(player)
            .map(currentRankNode -> {
                Set<String> walkedRankNodeIds = getWalkedRankNodeIds(player);
                return PREVIOUS_RANK_NODES_MAP
                    .get(currentRankNode)
                    .stream()
                    .filter(it -> walkedRankNodeIds.contains(it.getId()))
                    .toList();
            })
            .orElseGet(List::of);
    }

    public static Set<String> getWalkedRankNodeIds(@NotNull ServerPlayerEntity player) {
        return withRankDataNode(player, false, RankDataNode::getWalkedRankNodeIds);
    }

    public static void computeRankGraph() {
        NEXT_RANK_NODES_MAP.clear();
        PREVIOUS_RANK_NODES_MAP.clear();

        getAllRankNodes()
            .forEach(it -> {
                List<RankNode> nextRankNodes = computeNextRankNodes(it);
                NEXT_RANK_NODES_MAP.put(it, nextRankNodes);
                nextRankNodes.forEach(nextRankNode -> {
                    PREVIOUS_RANK_NODES_MAP
                        .computeIfAbsent(nextRankNode, k -> new ArrayList<>())
                        .add(it);
                });

                /* Create the dummy previous rank nodes for starting rank nodes. */
                PREVIOUS_RANK_NODES_MAP.computeIfAbsent(it, k -> new ArrayList<>());
            });

    }

    private static @NotNull List<RankNode> computeNextRankNodes(@NotNull RankNode it) {
        return it.getNextRankNodes()
            .stream()
            .map(id -> findRankNode(id).orElse(null))
            .filter(Objects::nonNull)
            .toList();
    }

}

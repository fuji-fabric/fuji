package io.github.sakurawald.fuji.module.initializer.rank.service;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.LuckpermsHelper;
import io.github.sakurawald.fuji.core.document.annotation.DocStringProvider;
import io.github.sakurawald.fuji.core.document.descriptor.PermissionDescriptor;
import io.github.sakurawald.fuji.module.initializer.rank.RankInitializer;
import io.github.sakurawald.fuji.module.initializer.rank.structure.RankNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

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

    public static Optional<RankNode> findRankNode(@NotNull String id) {
        return getAllRankNodes()
            .stream()
            .filter(it -> it.getId().equals(id))
            .findFirst();
    }

    public static Optional<RankNode> getStartingRankNode() {
        String startingRankNodeId = RankInitializer.config.model().getStartingRankNodeId();
        return findRankNode(startingRankNodeId);
    }

    public static List<RankNode> getAvailableStartingRankNodes(@NotNull ServerPlayerEntity player) {
        ArrayList<RankNode> result = new ArrayList<>();

        /* Get available starting rank nodes from permission. */
        List<RankNode> A = getAllRankNodes()
            .stream()
            .filter(it -> LuckpermsHelper.hasPermission(player.getUuid(), RANK_STARTING_RANK_NODE_PERMISSION_DESCRIPTOR, it.getId()))
            .toList();

        /* Get available starting rank node from the config. */
        getStartingRankNode()
            .ifPresent(result::add);

        /* Return the combined result. */
        return result;
    }

}

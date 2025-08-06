package io.github.sakurawald.fuji.module.initializer.rank.service;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.LuckpermsHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.executor.CommandExecutor;
import io.github.sakurawald.fuji.core.command.structure.ExtendedCommandSource;
import io.github.sakurawald.fuji.core.document.annotation.DocStringProvider;
import io.github.sakurawald.fuji.core.document.descriptor.PermissionDescriptor;
import io.github.sakurawald.fuji.core.manager.Managers;
import io.github.sakurawald.fuji.module.initializer.rank.RankInitializer;
import io.github.sakurawald.fuji.module.initializer.rank.structure.RankDataNode;
import io.github.sakurawald.fuji.module.initializer.rank.structure.RankNode;
import io.github.sakurawald.fuji.module.initializer.rank.structure.RankRequirement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
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

    @SuppressWarnings("UnnecessaryLocalVariable")
    public static boolean canMoveTo(@NotNull ServerPlayerEntity player, @NotNull RankNode rankNode) {
        ExtendedCommandSource source = ExtendedCommandSource.asConsole(player.getCommandSource());
        boolean canMoveTo = rankNode
            .getRequirements()
            .stream()
            .allMatch(rankRequirement -> isRankRequirementMet(rankRequirement, source));
        return canMoveTo;
    }

    public static boolean isRankRequirementMet(RankRequirement rankRequirement, ExtendedCommandSource source) {
        return rankRequirement
            .getCommands()
            .stream()
            .allMatch(command -> CommandHelper.Return.isSuccess(CommandExecutor.execute(source, command)));
    }

    public static void tryMoveTo(@NotNull ServerPlayerEntity player, @Nullable RankNode newRankNode) {
        /* Check requirements if new rank node is not null. */
        if (newRankNode != null) {
            if (canMoveTo(player, newRankNode)) {
                moveTo(player, newRankNode);
                TextHelper.sendTextByKey(player, "rank.up", newRankNode.getDisplayName());
            } else {
                TextHelper.sendTextByKey(player, "rank.up.requirements_not_meet", newRankNode.getDisplayName());
            }
        } else {
            moveTo(player, null);
        }
    }

    public static void moveTo(@NotNull ServerPlayerEntity player, @Nullable RankNode newRankNode) {
        ExtendedCommandSource source = ExtendedCommandSource.asConsole(player.getCommandSource());

        /* Execute the leave commands for previous rank node. */
        Optional<RankNode> previousRankNode = getCurrentRankNode(player);
        previousRankNode
            .ifPresent(it -> {
                CommandExecutor.execute(source, it.getEvents().getOnLeaveThisRankNodeCommands());
            });

        /* Save the state. */
        withRankDataNode(player, true, rankDataNode -> {
            String newValue = newRankNode == null ? null : newRankNode.getId();
            rankDataNode.setCurrentRankNodeId(newValue);

            /* If new rank node is not null. */
            if (newRankNode != null) {
                /* Execute the enter commands for new rank node. */
                CommandExecutor.execute(source, newRankNode.getEvents().getOnEnterThisRankNodeCommands());

                /* Is the first time to enter this rank node? */
                if (!rankDataNode.getWalkedRankNodeIds().contains(newValue)) {
                    CommandExecutor.execute(source, newRankNode.getEvents().getOnFirstEnterThisRankNodeCommands());
                }

                /* Remember this rank node. */
                rankDataNode.getWalkedRankNodeIds().add(newRankNode.getId());
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
        return getNextAvailableRankNodes(player, getCurrentRankNode(player));
    }

    public static @NotNull List<RankNode> getNextAvailableRankNodes(@NotNull ServerPlayerEntity player, Optional<RankNode> currentRankNode) {
        return currentRankNode
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

    public static void sendRankNodeInfo(@NotNull ServerCommandSource source, @NotNull RankNode rankNode, boolean displayRequirements) {
        /* Send basic info for the rank node. */
        TextHelper.sendTextByKey(source, "rank.info.header");
        TextHelper.sendTextByKey(source, "rank.rank_node.id", rankNode.getId());
        TextHelper.sendTextByKey(source, "rank.rank_node.display_name", rankNode.getDisplayName());
        TextHelper.sendTextByKey(source, "rank.rank_node.description", rankNode.getDescription());

        /* Send next rank nodes for the rank node. */
        List<String> nextRankNodes = rankNode.getNextRankNodes();
        if (nextRankNodes.isEmpty()) {
            TextHelper.sendTextByKey(source, "rank.rank_node.next_nodes", TextHelper.Operators.visitString(getNoRankStatusText()));
        } else {
            TextHelper.sendTextByKey(source, "rank.rank_node.next_nodes", nextRankNodes.toString());
        }

        /* Send requirements for the rank node. */
        if (displayRequirements) {
            TextHelper.sendTextByKey(source, "rank.rank_node.requirements");
            List<RankRequirement> requirements = rankNode.getRequirements();
            if (requirements.isEmpty()) {
                TextHelper.sendTextByKey(source, "rank.rank_node.requirements.empty");
            } else {
                requirements
                    .forEach(rankRequirement -> {
                        ExtendedCommandSource extendedCommandSource = ExtendedCommandSource.asConsole(source);
                        boolean rankRequirementMet = isRankRequirementMet(rankRequirement, extendedCommandSource);
                        String languageKey = rankRequirementMet ? "checkbox.true" : "checkbox.false";
                        TextHelper.sendTextByKey(source, languageKey, rankRequirement.getDescription());
                    });
            }
        }
    }

    public static int sendRankProgress(@NotNull ServerCommandSource source, @NotNull ServerPlayerEntity target, @Nullable RankNode specifiedRankNode) {
        Optional<RankNode> $specifiedRankNode;
        if (specifiedRankNode == null) {
            $specifiedRankNode = getCurrentRankNode(target);
        } else {
            $specifiedRankNode = Optional.of(specifiedRankNode);
        }

        return $specifiedRankNode
            .map(currentRankNode -> {
                /* Send current rank node info. */
                sendRankNodeInfo(source, currentRankNode, specifiedRankNode != null);

                /* Send click-able text for next rank nodes. */
                List<RankNode> nextRankNodes = getNextAvailableRankNodes(target, Optional.of(currentRankNode));
                if (!nextRankNodes.isEmpty()) {
                    TextHelper.sendTextByKey(source, "rank.progress.next_ranks");
                    MutableText textBuilder = Text.empty();
                    nextRankNodes
                        .forEach(nextRankNode -> {
                            String value = "<aqua>[%s]".formatted(nextRankNode.getDisplayName());
                            MutableText singleText = TextHelper.getTextByValue(source, value).copy();
                            ClickEvent clickEvent = Managers.getCallbackManager().makeCallbackEvent((player) -> {
                                sendRankProgress(source, target, nextRankNode);
                            }, 5, TimeUnit.MINUTES);
                            Text hoverText = TextHelper.getTextByKey(source, "prompt.click.see_it.any");
                            singleText
                                .fillStyle(Style.EMPTY
                                    .withClickEvent(clickEvent)
                                    .withHoverEvent(TextHelper.Events.HoverEvent.makeShowTextAction(hoverText)));

                            textBuilder.append(singleText);
                            textBuilder.append(TextHelper.TEXT_SPACE);
                        });
                    source.sendMessage(textBuilder);
                } else {
                    TextHelper.sendTextByKey(source, "rank.progress.no_next_rank");
                }
                return CommandHelper.Return.SUCCESS;
            })
            .orElseGet(() -> {
                String playerName = PlayerHelper.getPlayerName(target);
                TextHelper.sendTextByKey(source, "rank.progress.no_rank", playerName);
                return CommandHelper.Return.FAIL;
            });
    }
}

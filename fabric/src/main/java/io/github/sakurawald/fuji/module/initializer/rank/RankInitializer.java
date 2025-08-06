package io.github.sakurawald.fuji.module.initializer.rank;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.rank.command.argument.wrapper.NextAvailableRankNode;
import io.github.sakurawald.fuji.module.initializer.rank.command.argument.wrapper.PreviousAvailableRankNode;
import io.github.sakurawald.fuji.module.initializer.rank.config.model.RankConfigModel;
import io.github.sakurawald.fuji.module.initializer.rank.config.model.RankDataModel;
import io.github.sakurawald.fuji.module.initializer.rank.service.RankService;
import io.github.sakurawald.fuji.module.initializer.rank.structure.RankNode;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;


@Document(id = 1754411151804L, value = """
    This module provides the rank up system.
    You can define a `rank` with `requirements` and `award`.
    """)

@ColorBox(id = 1754450877863L, color = ColorBox.ColorBlockTypes.NOTE, value = """
    ◉ How it works?
    1. A `rank node` is used to define a `rank`.
    1.a. A `rank` has basic information, like `id`, `display name` and `description`.
    1.b. A `rank` can have multiple `next ranks`, to construct the `rank paths`.
    1.b.i. From `rank A` to `rank E`, you can define a `rank path` as `A -> B -> C -> D -> E`
    1.b.ii. From `rank A` to `rank E`, you can define another `rank path` as `A -> F -> G -> E`
    1.b.iii. A player can use `/rank up <next-rank>` to choose a `rank path` to `walk`.
    2. A `rank` can have `events`:
    2.a. The `on_enter_this_rank_node_commands` will be executed when a player `enter` this `rank`.
    2.b. The `on_lave_this_rank_node_commands` will be executed when a player `leave` this `rank`.
    2.c. The `on_first_enter_this_rank_node_commands` will be executed when a player `the first time enter` this `rank`.
    3. A `rank` can have `requirements`.
    3.a. A player must meet all the `requirements`, so that the `/rank up <next-rank>` command can be executed successfully.
    3.b. The admin can use `/rank set <player> <rank>` command to `force set` a player's rank. (Ignore requirements)
    3.c. A player can use `/rank down <rank>` to `rank down` to a previously `earned rank`.
    3.d. The admin can use `/rank remove <player>` to set a player's rank to `none`.
    """)
@ColorBox(id = 1754466653435L, color = ColorBox.ColorBlockTypes.TIPS, value = """
    ◉ Automatic rank up to the only available next rank.
    You can use `command_schedule` module to define a job.
    To execute the `/rank try-up %player:name%` command for each online player.
    So that each player will get auto rank up if there is only one `available next rank` for them.
    It can be `/execute as @a run rank try-up @s` or `/foreach rank try-up %player:name%`
    """)
@ColorBox(id = 1754451752816L, color = ColorBox.ColorBlockTypes.EXAMPLE, value = """
    ◉ List rank nodes by type
    1. `/rank list all-rank-nodes`
    2. `/rank list starting-rank-nodes Steve`
    3. `/rank list next-rank-nodes Steve`
    4. `/rank list previous-rank-nodes Steve`
    5. `/rank list walked-rank-nodes Steve`

    ◉ Query the info of a rank
    Issue: `/rank info newbie`

    ◉ Query current rank progress
    1. `/rank progress`
    2. `/rank progress Steve`

    ◉ Rank up to a specified rank
    Issue: `/rank up branch-1 --confirm true`

    ◉ Rank down to a `walked rank`
    Issue: `/rank down branch-1 --confirm true`
    <red>NOTE: If you `rank down` from this rank node, you must meet its `requirements` again before you can `rank back up` to it.

    ◉ Set a player's rank to none
    Issue: `/rank remove Steve --confirm true`.
    """)
@ColorBox(id = 1754465524369L, color = ColorBox.ColorBlockTypes.EXAMPLE, value = """
    ◉ Allow a player to choose an `intermediate rank` as their `starting rank`.
    You can define one `starting rank node` as the public `starting rank node` for all players.
    However, you can also allow some players to pick other `rank nodes` as their starting rank node, and skip some path.
    Issue: `/lp group default permission set fuji.rank.starting_rank_node.branch-1-3`
    This will allow players to pick `branch-1-3` as their `starting rank node`.
    You can define multiple `starting rank nodes` for different `luckperms groups`.
    """)
public class RankInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<RankConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, RankConfigModel.class);
    public static final BaseConfigurationHandler<RankDataModel> data = new ObjectConfigurationHandler<>("rank-data.json", RankDataModel.class);

    @Document(id = 1754412528895L, value = "List all defined `rank nodes`.")
    @CommandNode("rank list all-rank-nodes")
    @CommandRequirement(level = 4)
    private static int $listAllRankNodes(@CommandSource ServerCommandSource source) {
        TextHelper.sendTextByKey(source, "rank.list.all_rank_nodes", RankService.getAllRankIds());
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1754412670219L, value = "Query the info of the specified `rank node`.")
    @CommandNode("rank info")
    @CommandRequirement(level = 4)
    private static int $info(@CommandSource ServerCommandSource source, RankNode rankNode) {
        RankService.sendRankNodeInfo(source, rankNode, true);
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1754414840437L, value = "List all available `starting rank nodes` for the specified player.")
    @CommandNode("rank list starting-rank-nodes")
    @CommandRequirement(level = 4)
    private static int $listStartingRankNodes(@CommandSource ServerCommandSource source, ServerPlayerEntity target) {
        String playerName = PlayerHelper.getPlayerName(target);
        List<String> availableStartingRankNodes = RankService
            .getAvailableStartingRankNodes(target)
            .stream()
            .map(RankNode::getId)
            .toList();
        TextHelper.sendTextByKey(source, "rank.list.starting_rank_nodes", playerName, availableStartingRankNodes);
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1754415572673L, value = "Query the rank progress of the specified player.")
    @CommandNode("rank progress")
    @CommandRequirement(level = 4)
    private static int $rankProgress(@CommandSource ServerCommandSource source, ServerPlayerEntity target) {
        return RankService.getCurrentRankNode(target)
            .map(currentRankNode -> {
                RankService.sendRankNodeInfo(source, currentRankNode, false);
                return CommandHelper.Return.SUCCESS;
            })
            .orElseGet(() -> {
                String playerName = PlayerHelper.getPlayerName(target);
                TextHelper.sendTextByKey(source, "rank.progress.no_rank", playerName);
                return CommandHelper.Return.FAIL;
            });
    }

    @Document(id = 1754420858807L, value = "Query the rank progress.")
    @CommandNode("rank progress")
    private static int $rankProgress(@CommandSource ServerPlayerEntity player) {
        return $rankProgress(player.getCommandSource(), player);
    }

    @Document(id = 1754417962937L, value = "Set the rank for specified player.")
    @CommandNode("rank set")
    @CommandRequirement(level = 4)
    private static int $setRank(@CommandSource ServerCommandSource source, ServerPlayerEntity target, RankNode rankNode) {
        RankService.moveTo(target, rankNode);
        TextHelper.sendTextByKey(source, "rank.set", PlayerHelper.getPlayerName(target), rankNode.getId());
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1754418507342L, value = "Rank up to the next available rank node.")
    @CommandNode("rank up")
    private static int $rankUp(@CommandSource ServerPlayerEntity player, NextAvailableRankNode nextRank, Optional<Boolean> confirm) {
        return CommandHelper.Pattern.withCommandConfirmed(player, confirm, () -> {
            RankNode $nextRank = nextRank.getValue();
            RankService.tryMoveTo(player, $nextRank);
            return CommandHelper.Return.SUCCESS;
        });
    }

    @Document(id = 1754423529102L, value = "Rank down to the previous available rank node.")
    @CommandNode("rank down")
    private static int $rankDown(@CommandSource ServerPlayerEntity player, PreviousAvailableRankNode previousRank, Optional<Boolean> confirm) {
        return CommandHelper.Pattern.withCommandConfirmed(player, confirm, () -> {
            RankNode $previousRank = previousRank.getValue();
            RankService.moveTo(player, $previousRank);
            TextHelper.sendTextByKey(player, "rank.down", $previousRank.getDisplayName());
            return CommandHelper.Return.SUCCESS;
        });
    }

    @Document(id = 1754421296792L, value = "Set the specified player's rank to none.")
    @CommandNode("rank remove")
    @CommandRequirement(level = 4)
    private static int $removeRank(@CommandSource ServerCommandSource source, ServerPlayerEntity player, Optional<Boolean> confirm) {
        return CommandHelper.Pattern.withCommandConfirmed(source, confirm, () -> {
            RankService.moveTo(player, null);
            TextHelper.sendTextByKey(source, "rank.remove", PlayerHelper.getPlayerName(player));
            return CommandHelper.Return.SUCCESS;
        });
    }

    @Document(id = 1754424553830L, value = "List all available `next rank nodes` for the specified player.")
    @CommandNode("rank list next-rank-nodes")
    @CommandRequirement(level = 4)
    private static int $listNextRankNodes(@CommandSource ServerCommandSource source, ServerPlayerEntity target) {
        String playerName = PlayerHelper.getPlayerName(target);
        List<String> ids = RankService.getNextAvailableRankNodes(target).stream().map(RankNode::getId).toList();
        TextHelper.sendTextByKey(source, "rank.list.next_rank_nodes", playerName, ids);
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1754424619470L, value = "List all available `previous rank nodes` for the specified player.")
    @CommandNode("rank list previous-rank-nodes")
    @CommandRequirement(level = 4)
    private static int $listPreviousRankNodes(@CommandSource ServerCommandSource source, ServerPlayerEntity target) {
        String playerName = PlayerHelper.getPlayerName(target);
        List<String> ids = RankService.getPreviousAvailableRankNodes(target).stream().map(RankNode::getId).toList();
        TextHelper.sendTextByKey(source, "rank.list.previous_rank_nodes", playerName, ids);
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1754430022210L, value = "List all `walked rank nodes` for the specified player.")
    @CommandNode("rank list walked-rank-nodes")
    @CommandRequirement(level = 4)
    private static int $listWalkedRankNodes(@CommandSource ServerCommandSource source, ServerPlayerEntity target) {
        String playerName = PlayerHelper.getPlayerName(target);
        Set<String> ids = RankService.getWalkedRankNodeIds(target);
        TextHelper.sendTextByKey(source, "rank.list.walked_rank_nodes", playerName, ids);
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1754466143852L, value = "If there is only one `next rank node` for the player, then rank up to that node, else do nothing.")
    @CommandNode("rank try-up")
    @CommandRequirement(level = 4)
    private static int $tryUp(@CommandSource ServerCommandSource source, ServerPlayerEntity target) {
        List<RankNode> nextAvailableRankNodes = RankService.getNextAvailableRankNodes(target);
        if (nextAvailableRankNodes.size() != 1) {
            return CommandHelper.Return.FAIL;
        }

        NextAvailableRankNode theOnlyNode = new NextAvailableRankNode(nextAvailableRankNodes.get(0));
        $rankUp(target, theOnlyNode, Optional.of(true));
        return CommandHelper.Return.SUCCESS;
    }


    @Override
    protected void onInitialize() {
        RankService.computeRankGraph();
    }

    @Override
    protected void onReload() {
        RankService.computeRankGraph();
    }

    @Override
    protected void registerPlaceholders() {
        RankPlaceholders.registerRankIdPlaceholder();
        RankPlaceholders.registerRankDisplayNamePlaceholder();
        RankPlaceholders.registerRankDisplayNameRawPlaceholder();
    }
}

package io.github.sakurawald.module.initializer.command_toolbox.warp;

import io.github.sakurawald.core.annotation.Document;
import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.core.auxiliary.minecraft.RegistryHelper;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.command.annotation.CommandNode;
import io.github.sakurawald.core.command.annotation.CommandRequirement;
import io.github.sakurawald.core.command.annotation.CommandSource;
import io.github.sakurawald.core.command.annotation.CommandTarget;
import io.github.sakurawald.core.command.argument.wrapper.impl.GreedyString;
import io.github.sakurawald.core.command.exception.AbortCommandExecutionException;
import io.github.sakurawald.core.command.executor.CommandExecutor;
import io.github.sakurawald.core.command.structure.ExtendedCommandSource;
import io.github.sakurawald.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.core.service.string_splitter.StringSplitter;
import io.github.sakurawald.core.structure.SpatialPose;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import io.github.sakurawald.module.initializer.command_toolbox.warp.command.argument.wrapper.WarpName;
import io.github.sakurawald.module.initializer.command_toolbox.warp.config.model.WarpDataModel;
import io.github.sakurawald.module.initializer.command_toolbox.warp.gui.WarpGui;
import io.github.sakurawald.module.initializer.command_toolbox.warp.structure.WarpNode;
import net.minecraft.item.Item;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@CommandNode("warp")
public class WarpInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<WarpDataModel> data = new ObjectConfigurationHandler<>("warp.json", WarpDataModel.class)
        .autoSaveEveryMinute();

    private static void ensureWarpExists(ServerPlayerEntity player, WarpName warpName) {
        String name = warpName.getValue();
        if (!data.model().name2warp.containsKey(name)) {
            TextHelper.sendMessageByKey(player, "warp.not_found", name);
            throw new AbortCommandExecutionException();
        }
    }

    private static int withWarpNode(ServerPlayerEntity player, WarpName warpName, Function<WarpNode, Integer> consumer) {
        ensureWarpExists(player, warpName);
        String name = warpName.getValue();
        WarpNode entry = data.model().name2warp.get(name);
        return consumer.apply(entry);
    }

    public static void doWarp(WarpNode warpNode, ServerPlayerEntity player) {
        warpNode.getPosition().teleport(player);

        CommandExecutor.execute(
            ExtendedCommandSource.asConsole(player.getCommandSource())
            , warpNode.getEvent().on_warped.command_list);

        TextHelper.sendMessageByKey(player,"warp.tp.success",warpNode.name);
    }

    @CommandNode("tp")
    private static int $tp(@CommandSource @CommandTarget ServerPlayerEntity player, WarpName warpName) {
        return withWarpNode(player, warpName, warpNode -> {
            doWarp(warpNode, player);
            return CommandHelper.Return.SUCCESS;
        });
    }

    @CommandNode("unset")
    @CommandRequirement(level = 4)
    private static int $unset(@CommandSource ServerPlayerEntity player, WarpName warpName) {
        ensureWarpExists(player, warpName);

        String name = warpName.getValue();
        data.model().name2warp.remove(name);
        TextHelper.sendMessageByKey(player, "warp.unset.success", name);
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("set")
    @CommandRequirement(level = 4)
    private static int $set(@CommandSource ServerPlayerEntity player, WarpName warpName, Optional<Boolean> override) {
        String name = warpName.getValue();

        if (data.model().name2warp.containsKey(name)) {
            if (!override.orElse(false)) {
                TextHelper.sendMessageByKey(player, "warp.set.fail.need_override", name);
                return CommandHelper.Return.FAIL;
            }
        }

        WarpNode value = new WarpNode(SpatialPose.of(player))
            .withName(name);
        data.model().name2warp.put(name, value);
        TextHelper.sendMessageByKey(player, "warp.set.success", name);
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode()
    private static int $root(@CommandSource ServerCommandSource source) {
        return $list(source);
    }

    @CommandNode("list")
    private static int $list(@CommandSource ServerCommandSource source) {
        if (source.isExecutedByPlayer()) {
            List<WarpNode> list = data.model().name2warp.values().stream().toList();
            new WarpGui(source.getPlayer(), list, 0).open();
        } else {
            TextHelper.sendMessageByKey(source, "warp.list", data.model().name2warp.keySet());
        }

        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("set-name")
    @CommandRequirement(level = 4)
    @Document("Set the display name for a warp.")
    private static int $setName(@CommandSource ServerPlayerEntity player, WarpName warp, GreedyString name) {
        return withWarpNode(player, warp, warpNode -> {
            warpNode.setName(name.getValue());
            return CommandHelper.Return.SUCCESS;
        });
    }

    @CommandNode("set-item")
    @CommandRequirement(level = 4)
    @Document("Set the item for a warp.")
    private static int $setItem(@CommandSource ServerPlayerEntity player, WarpName warp, Item item) {
        return withWarpNode(player, warp, warpNode -> {
            warpNode.setItem(RegistryHelper.ofString(item));
            return CommandHelper.Return.SUCCESS;
        });
    }

    @CommandNode("set-lore")
    @CommandRequirement(level = 4)
    @Document("Set the lore for a warp.")
    private static int $setLore(@CommandSource ServerPlayerEntity player, WarpName warp, GreedyString lore) {
        return withWarpNode(player, warp, warpNode -> {
            List<String> split = StringSplitter.split(lore.getValue());
            warpNode.setLore(split);
            return CommandHelper.Return.SUCCESS;
        });
    }
}

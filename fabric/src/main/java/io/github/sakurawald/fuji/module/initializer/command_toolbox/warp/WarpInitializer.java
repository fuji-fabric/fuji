package io.github.sakurawald.fuji.module.initializer.command_toolbox.warp;

import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.command.annotation.CommandTarget;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.GreedyString;
import io.github.sakurawald.fuji.core.command.exception.AbortCommandExecutionException;
import io.github.sakurawald.fuji.core.command.executor.CommandExecutor;
import io.github.sakurawald.fuji.core.command.executor.structure.ExtendedCommandSource;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.service.string_splitter.StringSplitter;
import io.github.sakurawald.fuji.core.structure.GlobalPos;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.command_toolbox.warp.command.argument.wrapper.WarpName;
import io.github.sakurawald.fuji.module.initializer.command_toolbox.warp.config.model.WarpDataModel;
import io.github.sakurawald.fuji.module.initializer.command_toolbox.warp.gui.WarpGui;
import io.github.sakurawald.fuji.module.initializer.command_toolbox.warp.structure.WarpNode;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Document(id = 1751825390723L, value = """
    Provides `/warp` command.
    As a public teleport point.
    """)
@ColorBox(id = 1751972643858L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ Set a display name for a warp.
    Issue: `/warp set-name \\<warp\\> \\<blue\\>This is the display name`

    ◉ Set a lore for a warp.
    Issue: `/warp set-lore \\<warp\\> \\<blue\\>This is the first line|<red>This is the second line`

    """)


@CommandNode("warp")
public class WarpInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<WarpDataModel> data = new ObjectConfigurationHandler<>("warp.json", WarpDataModel.class)
        .enableAutoSaveFeature();

    private static void ensureWarpExists(ServerPlayerEntity player, WarpName warpName) {
        String name = warpName.getValue();
        if (!data.model().name2warp.containsKey(name)) {
            TextHelper.sendTextByKey(player, "warp.not_found", name);
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

        TextHelper.sendTextByKey(player,"warp.tp.success",warpNode.name);
    }

    @Document(id = 1751825396093L, value = "Teleport to the specified warp point.")
    @CommandNode("tp")
    private static int $tp(@CommandSource @CommandTarget ServerPlayerEntity player, WarpName warpName) {
        return withWarpNode(player, warpName, warpNode -> {
            doWarp(warpNode, player);
            return CommandHelper.Return.SUCCESS;
        });
    }

    @Document(id = 1751825400830L, value = "Delete a warp point.")
    @CommandNode("unset")
    @CommandRequirement(level = 4)
    private static int $unset(@CommandSource ServerPlayerEntity player, WarpName warpName) {
        ensureWarpExists(player, warpName);

        String name = warpName.getValue();
        data.model().name2warp.remove(name);
        TextHelper.sendTextByKey(player, "warp.unset.success", name);
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751825406388L, value = "Set current location as new wrap point.")
    @CommandNode("set")
    @CommandRequirement(level = 4)
    private static int $set(@CommandSource ServerPlayerEntity player, WarpName warpName, Optional<Boolean> override) {
        String name = warpName.getValue();

        if (data.model().name2warp.containsKey(name)) {
            if (!override.orElse(false)) {
                TextHelper.sendTextByKey(player, "warp.set.fail.need_override", name);
                return CommandHelper.Return.FAILURE;
            }
        }

        WarpNode value = new WarpNode(GlobalPos.of(player))
            .withName(name);
        data.model().name2warp.put(name, value);
        TextHelper.sendTextByKey(player, "warp.set.success", name);
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode
    private static int $root(@CommandSource ServerCommandSource source) {
        return $list(source);
    }

    @Document(id = 1751825410558L, value = "List warp points.")
    @CommandNode("list")
    private static int $list(@CommandSource ServerCommandSource source) {
        if (source.isExecutedByPlayer()) {
            List<WarpNode> list = data.model().name2warp.values().stream().toList();
            new WarpGui(source.getPlayer(), list, 0).open();
        } else {
            TextHelper.sendTextByKey(source, "warp.list", data.model().name2warp.keySet());
        }

        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751825417554L, value = "Set the display name for a warp.")
    @CommandNode("set-name")
    @CommandRequirement(level = 4)
    private static int $setName(@CommandSource ServerPlayerEntity player, WarpName warp, GreedyString name) {
        return withWarpNode(player, warp, warpNode -> {
            warpNode.setName(name.getValue());
            return CommandHelper.Return.SUCCESS;
        });
    }

    @Document(id = 1751825422424L, value = "Set the item for a warp.")
    @CommandNode("set-item")
    @CommandRequirement(level = 4)
    private static int $setItem(@CommandSource ServerPlayerEntity player, WarpName warp, GreedyString itemString) {
        return withWarpNode(player, warp, warpNode -> {
            warpNode.setItem(itemString.getValue());
            return CommandHelper.Return.SUCCESS;
        });
    }

    @Document(id = 1751825427963L, value = "Set the lore for a warp.")
    @CommandNode("set-lore")
    @CommandRequirement(level = 4)
    private static int $setLore(@CommandSource ServerPlayerEntity player, WarpName warp, GreedyString lore) {
        return withWarpNode(player, warp, warpNode -> {
            List<String> split = StringSplitter.split(lore.getValue());
            warpNode.setLore(split);
            return CommandHelper.Return.SUCCESS;
        });
    }
}

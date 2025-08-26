package io.github.sakurawald.fuji.module.initializer.command_toolbox.warp;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.command.annotation.CommandTarget;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.GreedyString;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.ItemStackWrapper;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.service.string_splitter.StringSplitter;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.command_toolbox.warp.config.model.WarpDataModel;
import io.github.sakurawald.fuji.module.initializer.command_toolbox.warp.gui.WarpGui;
import io.github.sakurawald.fuji.module.initializer.command_toolbox.warp.service.WarpService;
import io.github.sakurawald.fuji.module.initializer.command_toolbox.warp.structure.WarpDescriptor;
import java.util.List;
import java.util.Optional;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

@Document(id = 1751825390723L, value = """
    Provides `/warp` command.
    As a public teleport point.
    """)
@ColorBox(id = 1751972643858L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ Set a display name for a warp.
    Issue: `/warp set-name \\<warp\\> \\<blue\\>This is the display name`

    ◉ Set a lore for a warp.
    Issue: `/warp set-lore \\<warp\\> \\<blue\\>This is the first line|\\<red\\>This is the second line`
    """)
@CommandNode("warp")
public class WarpInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<WarpDataModel> data = ObjectConfigurationHandler
        .ofModule("warp.json", WarpDataModel.class)
        .enableAutoSaveFeature();

    @Document(id = 1751825396093L, value = "Teleport to the specified warp point.")
    @CommandNode("tp")
    private static int $tp(@CommandSource @CommandTarget ServerPlayerEntity player, WarpDescriptor warpName) {
        WarpService.doWarp(warpName, player);
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751825400830L, value = "Delete a warp point.")
    @CommandNode("unset")
    @CommandRequirement(level = 4)
    private static int $unset(@CommandSource ServerPlayerEntity player, WarpDescriptor warpName) {
        return WarpService
            .deleteWarp(warpName)
            .map(unused -> {
                TextHelper.sendTextByKey(player, "warp.unset.success", warpName.getKey());
                return CommandHelper.Return.SUCCESS;
            })
            .orElse(CommandHelper.Return.FAILURE);
    }

    @Document(id = 1751825406388L, value = "Set current location as new wrap point.")
    @CommandNode("set")
    @CommandRequirement(level = 4)
    private static int $set(@CommandSource ServerPlayerEntity player, String warpName, Optional<Boolean> override) {
        if (WarpService.findWarp(warpName).isPresent()) {
            if (!override.orElse(false)) {
                TextHelper.sendTextByKey(player, "warp.set.fail.need_override", warpName);
                return CommandHelper.Return.FAILURE;
            }
        }

        WarpService.createWarp(player, warpName);
        TextHelper.sendTextByKey(player, "warp.set.success", warpName);
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode
    private static int $root(@CommandSource ServerCommandSource source) {
        return $list(source);
    }

    @Document(id = 1751825410558L, value = "List warp points.")
    @CommandNode("list")
    private static int $list(@CommandSource ServerCommandSource source) {
        return Optional
            .ofNullable(source.getPlayer())
            .map(player -> {
                WarpGui.makeDefault(player).open();
                return CommandHelper.Return.SUCCESS;
            })
            .orElseGet(() -> {
                TextHelper.sendTextByKey(source, "warp.list", data.model().warps.keySet());
                return CommandHelper.Return.SUCCESS;
            });
    }

    @Document(id = 1751825417554L, value = "Set the display name for a warp.")
    @CommandNode("set-name")
    @CommandRequirement(level = 4)
    private static int $setName(@CommandSource ServerPlayerEntity player, WarpDescriptor warp, GreedyString name) {
        warp.setDisplayName(name.getValue());
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751825422424L, value = "Set the item for a warp.")
    @CommandNode("set-item")
    @CommandRequirement(level = 4)
    private static int $setItem(@CommandSource ServerPlayerEntity player, WarpDescriptor warp, ItemStackWrapper itemStack) {
        warp.setItem(itemStack.getInputString());
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751825427963L, value = "Set the lore for a warp.")
    @CommandNode("set-lore")
    @CommandRequirement(level = 4)
    private static int $setLore(@CommandSource ServerPlayerEntity player, WarpDescriptor warp, GreedyString lore) {
        List<String> split = StringSplitter.split(lore.getValue());
        warp.setLore(split);
        return CommandHelper.Return.SUCCESS;
    }
}

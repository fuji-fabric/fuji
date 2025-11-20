package mod.fuji.module.initializer.command_toolbox.warp;

import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandRequirement;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.core.command.annotation.CommandTarget;
import mod.fuji.core.command.argument.wrapper.impl.GreedyString;
import mod.fuji.core.command.argument.wrapper.impl.ItemStackWrapper;
import mod.fuji.core.config.handler.abst.BaseConfigurationHandler;
import mod.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import mod.fuji.core.document.annotation.ColorBox;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.job.JobManager;
import mod.fuji.core.service.string_splitter.StringSplitter;
import mod.fuji.core.structure.GlobalPos;
import mod.fuji.module.initializer.ModuleInitializer;
import mod.fuji.module.initializer.command_toolbox.warp.config.model.WarpDataModel;
import mod.fuji.module.initializer.command_toolbox.warp.gui.WarpGui;
import mod.fuji.module.initializer.command_toolbox.warp.service.WarpService;
import mod.fuji.module.initializer.command_toolbox.warp.structure.WarpDescriptor;
import java.util.List;
import java.util.Optional;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

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
        .enableAutoSaveFeature(JobManager.CRON_EVERY_MINUTE);

    @Document(id = 1751825396093L, value = "Teleport to the specified warp point.")
    @CommandNode("tp")
    private static int $tp(@CommandSource @CommandTarget ServerPlayer player, WarpDescriptor warpName) {
        WarpService.doWarp(warpName, player);
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751825400830L, value = "Delete a warp point.")
    @CommandNode("unset")
    @CommandRequirement(level = 4)
    private static int $unset(@CommandSource ServerPlayer player, WarpDescriptor warpName) {
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
    private static int $set(@CommandSource ServerPlayer player, String warpName, Optional<Boolean> override) {
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
    private static int $root(@CommandSource CommandSourceStack source) {
        return $list(source);
    }

    @Document(id = 1751825410558L, value = "List warp points.")
    @CommandNode("list")
    private static int $list(@CommandSource CommandSourceStack source) {
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
    private static int $setName(@CommandSource ServerPlayer player, WarpDescriptor warp, GreedyString name) {
        warp.setDisplayName(name.getValue());
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751825422424L, value = "Set the item for a warp.")
    @CommandNode("set-item")
    @CommandRequirement(level = 4)
    private static int $setItem(@CommandSource ServerPlayer player, WarpDescriptor warp, ItemStackWrapper itemStack) {
        warp.setItem(itemStack.getInputString());
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751825427963L, value = "Set the lore for a warp.")
    @CommandNode("set-lore")
    @CommandRequirement(level = 4)
    private static int $setLore(@CommandSource ServerPlayer player, WarpDescriptor warp, GreedyString lore) {
        List<String> split = StringSplitter.split(lore.getValue());
        warp.setLore(split);
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1756241454472L, value = "Set the position for a warp.")
    @CommandNode("set-position")
    @CommandRequirement(level = 4)
    private static int $setPosition(@CommandSource ServerPlayer player, WarpDescriptor warp) {
        GlobalPos newPosition = GlobalPos.of(player);
        warp.setPosition(newPosition);
        return CommandHelper.Return.SUCCESS;
    }
}

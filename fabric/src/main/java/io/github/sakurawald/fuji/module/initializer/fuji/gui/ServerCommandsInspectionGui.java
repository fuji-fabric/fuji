package io.github.sakurawald.fuji.module.initializer.fuji.gui;

import com.mojang.brigadier.tree.CommandNode;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.GuiHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.gui.impl.gui.PagedGui;
import io.github.sakurawald.fuji.module.initializer.fuji.structure.ServerCommandNodeWrapper;
import net.minecraft.item.Items;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ServerCommandsInspectionGui extends PagedGui<ServerCommandNodeWrapper> {

    public ServerCommandsInspectionGui(ServerPlayerEntity player, @NotNull List<ServerCommandNodeWrapper> entities, int pageIndex) {
        super(null, player, TextHelper.getTextByKey(player, "fuji.inspect.server_commands.gui.title"), entities, pageIndex);

        getFooter().setSlot(4, GuiHelper.makeHelpButton(player)
            .setLore(TextHelper.getTextListByKey(player, "fuji.inspect.server_commands.gui.help.lore")));
    }

    public static ServerCommandsInspectionGui inspectAll(ServerPlayerEntity player) {
        List<ServerCommandNodeWrapper> entities = CommandHelper.getCommandNodes()
            .stream()
            .map(ServerCommandNodeWrapper::new)
            .sorted(Comparator.comparing(ServerCommandNodeWrapper::getPath))
            .toList();
        return new ServerCommandsInspectionGui(player, entities, 0);
    }

    @Override
    protected PagedGui<ServerCommandNodeWrapper> make(@Nullable SimpleGui parent, ServerPlayerEntity player, Text title, @NotNull List<ServerCommandNodeWrapper> entities, int pageIndex) {
        return new ServerCommandsInspectionGui(player, entities, pageIndex);
    }

    @Override
    protected GuiElementInterface toGuiElement(ServerCommandNodeWrapper entity) {
        List<Text> lore = new ArrayList<>();

        /* Guess what package the command node is. */
        lore.add(TextHelper.getTextByKey(getPlayer(), "from_package", entity.fromPackage));

        String commandPath = entity.getPath();
        return new GuiElementBuilder()
            .setItem(Items.COMMAND_BLOCK)
            .setName(Text.literal(commandPath))
            .setLore(lore)
            .setCallback((index, clickType, actionType) -> {
                TextHelper.sendTextByKey(getPlayer(), "fuji.inspect.server_commands.gui.copy_command_path", commandPath, commandPath);
                close();
            })
            .build();
    }

}

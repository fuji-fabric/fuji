package mod.fuji.module.initializer.fuji.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.GuiHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.gui.component.gui.PagedGui;
import mod.fuji.core.gui.structure.GuiElementIR;
import mod.fuji.module.initializer.fuji.structure.ServerCommandNodeWrapper;
import net.minecraft.world.item.Items;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ServerCommandsInspectionGui extends PagedGui<ServerCommandNodeWrapper> {

    public ServerCommandsInspectionGui(ServerPlayer player, @NotNull List<ServerCommandNodeWrapper> entities, int pageIndex) {
        super(null, player, TextHelper.getTextByKey(player, "fuji.inspect.server_commands.gui.title"), entities, pageIndex);

        GuiHelper.Placer.setSlotInLastLine(this, 4, GuiHelper.Button.makeHelpButton(player)
            .setLore(TextHelper.getTextListByKey(player, "fuji.inspect.server_commands.gui.help.lore")));
    }

    public static ServerCommandsInspectionGui inspectAll(ServerPlayer player) {
        List<ServerCommandNodeWrapper> entities = CommandHelper.Tree.getAllCommandNodes()
            .stream()
            .map(ServerCommandNodeWrapper::new)
            .sorted(Comparator.comparing(ServerCommandNodeWrapper::getPath))
            .toList();
        return new ServerCommandsInspectionGui(player, entities, 0);
    }

    @Override
    protected @NotNull PagedGui<ServerCommandNodeWrapper> makePage(@Nullable SimpleGui parent, @NotNull ServerPlayer player, Component title, @NotNull List<ServerCommandNodeWrapper> entities, int pageIndex) {
        return new ServerCommandsInspectionGui(player, entities, pageIndex);
    }

    @Override
    protected @NotNull GuiElementIR toGuiElement(@NotNull ServerCommandNodeWrapper entity) {
        List<Component> lore = new ArrayList<>();

        /* Guess what package the command node is. */
        lore.add(TextHelper.getTextByKey(getPlayer(), "from_package", entity.fromPackage));

        String commandPath = entity.getPath();
        return GuiElementIR.of(new GuiElementBuilder()
            .setItem(Items.COMMAND_BLOCK)
            .setName(Component.literal(commandPath))
            .setLore(lore)
            .setCallback((index, clickType, actionType) -> {
                TextHelper.sendTextByKey(getPlayer(), "fuji.inspect.server_commands.gui.copy_command_path", commandPath, commandPath);
                close();
            })
            .build());
    }

}

package io.github.sakurawald.module.initializer.command_permission.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.sakurawald.core.auxiliary.minecraft.GuiHelper;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.gui.PagedGui;
import io.github.sakurawald.module.initializer.command_permission.CommandPermissionInitializer;
import io.github.sakurawald.module.initializer.command_permission.structure.CommandNodePermission;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CommandPermissionGui extends PagedGui<CommandNodePermission> {

    public CommandPermissionGui(ServerPlayerEntity player, @NotNull List<CommandNodePermission> entities, int pageIndex) {
        super(null, player, TextHelper.getTextByKey(player, "command_permission.list.gui.title"), entities, pageIndex);

        getFooter().setSlot(4, GuiHelper.makeHelpButton(player)
            .setLore(TextHelper.getTextListByKey(player, "command_permission.list.gui.help.lore")));
    }

    @Override
    protected PagedGui<CommandNodePermission> make(@Nullable SimpleGui parent, ServerPlayerEntity player, Text title, @NotNull List<CommandNodePermission> entities, int pageIndex) {
        return new CommandPermissionGui(player, entities, pageIndex);
    }

    @Override
    protected GuiElementInterface toGuiElement(CommandNodePermission entity) {
        List<Text> lore = List.of(TextHelper.getTextByKey(getPlayer(), "command_permission.list.gui.entry.lore", entity.isWrapped()));

        return new GuiElementBuilder()
            .setItem(entity.isWrapped() ? Items.GREEN_STAINED_GLASS : Items.RED_STAINED_GLASS)
            .setName(Text.literal(entity.getPath()))
            .setCallback((index, clickType, actionType) -> {
                String commandPath = entity.getPath();
                String commandPermission = CommandPermissionInitializer.computeCommandPermission(commandPath);

                if (clickType.isLeft) {
                    String executionCommand = "/lp group default permission set %s true".formatted(commandPermission);
                    TextHelper.sendMessageByKey(getPlayer(), "command_permission.command.set_true", commandPath, executionCommand, executionCommand);
                } else if (clickType.isRight) {
                    String executionCommand = "/lp group default permission set %s false".formatted(commandPermission);
                    TextHelper.sendMessageByKey(getPlayer(), "command_permission.command.set_false", commandPath, executionCommand, executionCommand);
                } else if (clickType.isMiddle) {
                    String executionCommand = "/lp group default permission unset %s".formatted(commandPermission);
                    TextHelper.sendMessageByKey(getPlayer(), "command_permission.command.unset", commandPath, executionCommand, executionCommand);
                }

                close();
            })
            .setLore(lore)
            .build();
    }

    @Override
    protected List<CommandNodePermission> filter(String keyword) {
        return getEntities().stream()
            .filter(it -> it.getPath().contains(keyword))
            .toList();
    }
}

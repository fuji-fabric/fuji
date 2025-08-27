package io.github.sakurawald.fuji.module.initializer.command_permission.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.GuiHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.gui.component.gui.PagedGui;
import io.github.sakurawald.fuji.module.initializer.command_permission.CommandPermissionInitializer;
import io.github.sakurawald.fuji.module.initializer.command_permission.service.CommandPermissionService;
import io.github.sakurawald.fuji.module.initializer.command_permission.structure.CommandNodePermissionWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CommandPermissionGui extends PagedGui<CommandNodePermissionWrapper> {

    public CommandPermissionGui(ServerPlayerEntity player, @NotNull List<CommandNodePermissionWrapper> entities, int pageIndex) {
        super(null, player, TextHelper.getTextByKey(player, "command_permission.list.gui.title"), entities, pageIndex);

        GuiHelper.Placer.setSlotInLastLine(this, 4, GuiHelper.Button.makeHelpButton(player)
            .setLore(TextHelper.getTextListByKey(player, "command_permission.list.gui.help.lore")));
    }

    @Override
    protected PagedGui<CommandNodePermissionWrapper> makePage(@Nullable SimpleGui parent, @NotNull ServerPlayerEntity player, Text title, @NotNull List<CommandNodePermissionWrapper> entities, int pageIndex) {
        return new CommandPermissionGui(player, entities, pageIndex);
    }

    @Override
    protected @NotNull GuiElementInterface toGuiElement(@NotNull CommandNodePermissionWrapper entity) {
        boolean commandNodeWrapped = CommandPermissionService.isCommandNodeWrapped(entity.getNode());

        return new GuiElementBuilder()
            .setItem(GuiHelper.Material.fromBooleanValue(commandNodeWrapped))
            .setName(Text.literal(entity.getPath()))
            .setCallback((index, clickType, actionType) -> {
                String commandPathString = entity.getPath();
                String commandPermissionString = CommandPermissionInitializer.COMMAND_PERMISSION_UNIFIED_PERMISSION.withArguments(commandPathString);

                if (clickType.isLeft) {
                    String executionCommand = "/lp group default permission set %s true".formatted(commandPermissionString);
                    TextHelper.sendTextByKey(getPlayer(), "command_permission.command.set_true", commandPathString, executionCommand, executionCommand);
                } else if (clickType.isRight) {
                    String executionCommand = "/lp group default permission set %s false".formatted(commandPermissionString);
                    TextHelper.sendTextByKey(getPlayer(), "command_permission.command.set_false", commandPathString, executionCommand, executionCommand);
                } else if (clickType.isMiddle) {
                    String executionCommand = "/lp group default permission unset %s".formatted(commandPermissionString);
                    TextHelper.sendTextByKey(getPlayer(), "command_permission.command.unset", commandPathString, executionCommand, executionCommand);
                }

                close();
            })
            .setLore(List.of(TextHelper.getTextByKey(getPlayer(), "command_permission.list.gui.entry.lore", commandNodeWrapped)))
            .build();
    }

}

package io.github.sakurawald.core.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.command.structure.CommandDescriptor;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CommandDescriptorGui extends PagedGui<CommandDescriptor> {

    public CommandDescriptorGui(ServerPlayerEntity player, @NotNull List<CommandDescriptor> entities, int pageIndex) {
        super(null, player, TextHelper.getTextByKey(player, "fuji.inspect.fuji_commands.gui.title"), entities, pageIndex);
    }

    @Override
    protected PagedGui<CommandDescriptor> make(@Nullable SimpleGui parent, ServerPlayerEntity player, Text title, @NotNull List<CommandDescriptor> entities, int pageIndex) {
        return new CommandDescriptorGui(player, entities, pageIndex);
    }

    private List<Text> computeLore(CommandDescriptor entity) {
        List<Text> lore = new ArrayList<>();

        /* method document */
        if (entity.document != null) {
            lore.add(Text.literal(entity.document).formatted(Formatting.GOLD));
        }

        /* parameters document */
        lore.addAll(entity.arguments.stream()
            .filter(it -> it.getDocument() != null)
            .map(it -> (Text) Text.literal("%s -> %s".formatted(it.getArgumentName(), it.getDocument()))
                .formatted(Formatting.DARK_GREEN)).toList());

        return lore;
    }

    @Override
    protected GuiElementInterface toGuiElement(CommandDescriptor entity) {
        List<Text> lore = new ArrayList<>();
        lore.addAll(List.of(
            TextHelper.getTextByKey(getPlayer(), "command.source.can_be_executed_by_console", entity.canBeExecutedByConsole())
            , TextHelper.getTextByKey(getPlayer(), "command.descriptor.type", entity.getClass().getSimpleName())
            , TextHelper.getTextByKey(getPlayer(), "command.requirement.level_permission", entity.getDefaultLevelPermission())
            , TextHelper.getTextByKey(getPlayer(), "command.requirement.string_permission", entity.getDefaultStringPermission())
        ));
        lore.addAll(computeLore(entity));

        return new GuiElementBuilder()
            .setName(Text.literal(entity.getCommandSyntax()))
            .setItem(Items.REPEATING_COMMAND_BLOCK)
            .setLore(lore)
            .build();
    }

    @Override
    protected List<CommandDescriptor> filter(String keyword) {
        return getEntities().stream()
            .filter(it -> it.toString().contains(keyword)).toList();
    }
}

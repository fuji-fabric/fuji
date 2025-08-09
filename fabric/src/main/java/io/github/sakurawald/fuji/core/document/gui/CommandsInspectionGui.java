package io.github.sakurawald.fuji.core.document.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.processor.CommandAnnotationProcessor;
import io.github.sakurawald.fuji.core.command.structure.CommandDescriptor;
import io.github.sakurawald.fuji.core.gui.component.gui.PagedGui;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CommandsInspectionGui extends PagedGui<CommandDescriptor> {

    public CommandsInspectionGui(@Nullable SimpleGui parent, @NotNull ServerPlayerEntity player, @NotNull List<CommandDescriptor> entities, int pageIndex) {
        super(parent, player, TextHelper.getTextByKey(player, "fuji.inspect.fuji_commands.gui.title"), entities, pageIndex);
    }

    public static CommandsInspectionGui inspectAll(SimpleGui parent, ServerPlayerEntity player) {
        List<CommandDescriptor> entities = CommandAnnotationProcessor
            .REGISTERED_COMMAND_DESCRIPTORS
            .stream()
            .sorted(Comparator.comparing(CommandDescriptor::getCommandNodePath))
            .toList();

        return new CommandsInspectionGui(parent, player, entities, 0);
    }

    @Override
    protected PagedGui<CommandDescriptor> make(@Nullable SimpleGui parent, @NotNull ServerPlayerEntity player, Text title, @NotNull List<CommandDescriptor> entities, int pageIndex) {
        return new CommandsInspectionGui(parent, player, entities, pageIndex);
    }

    private @NotNull List<Text> asLore(@NotNull CommandDescriptor entity) {
        List<Text> lore = new ArrayList<>();

        /* Attach method document. */
        if (entity.document != null) {
            List<Text> methodDocumentTextList = TextHelper.getDocumentTextList(getPlayer(), entity.document);
            lore.addAll(methodDocumentTextList);
        }

        /* Attach parameters document. */
        List<Text> parameterDocumentTextList = entity.arguments
            .stream()
            .filter(it -> it.getDocument() != null)
            .map(it -> {
                Text documentText = TextHelper.getDocumentText(getPlayer(), "◉ %s: %s".formatted(it.getArgumentName(), it.getDocument()));
                return documentText;
            }).toList();
        lore.addAll(parameterDocumentTextList);

        return lore;
    }

    @SuppressWarnings("CollectionAddAllCanBeReplacedWithConstructor")
    @Override
    protected @NotNull GuiElementInterface toGuiElement(@NotNull CommandDescriptor entity) {
        List<Text> lore = new ArrayList<>();

        /* Add basic properties of command descriptor. */
        lore.addAll(List.of(
            TextHelper.getTextByKey(getPlayer(),"from_module", entity.getSourceModule())
            , TextHelper.getTextByKey(getPlayer(), "command.source.can_be_executed_by_console", entity.canBeExecutedByConsole())
            , TextHelper.getTextByKey(getPlayer(), "command.descriptor.type", entity.getClass().getSimpleName())
            , TextHelper.getTextByKey(getPlayer(), "command.requirement.level_permission", entity.getDefaultLevelPermission())
            , TextHelper.getTextByKey(getPlayer(), "command.requirement.string_permission", entity.getDefaultStringPermission())
        ));

        /* Add documents lore of this command. */
        List<Text> documents = asLore(entity);
        if (!documents.isEmpty()) {
            documents.add(0, Text.empty());
            lore.addAll(documents);
        }

        /* Make the GUI. */
        return new GuiElementBuilder()
            .setName(Text.literal(entity.getCommandSyntax()))
            .setItem(Items.REPEATING_COMMAND_BLOCK)
            .setLore(lore)
            .build();
    }

}

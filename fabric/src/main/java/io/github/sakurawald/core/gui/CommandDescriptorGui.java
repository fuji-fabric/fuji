package io.github.sakurawald.core.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.command.structure.CommandDescriptor;
import io.github.sakurawald.core.manager.impl.module.ModuleManager;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
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

    private List<Text> computeDocumentsLore(CommandDescriptor entity) {
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

    @Override
    protected GuiElementInterface toGuiElement(CommandDescriptor entity) {
        List<Text> lore = new ArrayList<>();

        /* Add basic properties of command descriptor. */
        String sourceModule = ModuleManager.computeModulePathAsString(entity.method.getDeclaringClass().getName());
        lore.addAll(List.of(
            TextHelper.getTextByKey(getPlayer(),"from_module", sourceModule)
            , TextHelper.getTextByKey(getPlayer(), "command.source.can_be_executed_by_console", entity.canBeExecutedByConsole())
            , TextHelper.getTextByKey(getPlayer(), "command.descriptor.type", entity.getClass().getSimpleName())
            , TextHelper.getTextByKey(getPlayer(), "command.requirement.level_permission", entity.getDefaultLevelPermission())
            , TextHelper.getTextByKey(getPlayer(), "command.requirement.string_permission", entity.getDefaultStringPermission())
        ));

        /* Add documents lore of this command. */
        List<Text> documents = computeDocumentsLore(entity);
        if (!documents.isEmpty()) {
            documents.add(0, Text.empty());
            lore.addAll(documents);
        }

        /* Make the GUI. */
        return new GuiElementBuilder()
            .setName(Text.literal(entity.getCommandSyntax()))
            .setItem(Items.REPEATING_COMMAND_BLOCK)
            .setLore(lore)
            .setCallback(() -> handleClick(getPlayer(), entity, sourceModule))
            .build();
    }

    private void handleClick(ServerPlayerEntity player, CommandDescriptor entity, String sourceModule) {

        ModulesInspectionGui.makeDefault(player)
            .search(sourceModule)
            .open();

    }

    @Override
    protected List<CommandDescriptor> filter(String keyword) {
        return getEntities().stream()
            .filter(it -> it.toString().contains(keyword)
            || it.method.getDeclaringClass().getName().contains(keyword))
            .toList();
    }
}

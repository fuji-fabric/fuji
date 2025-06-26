package io.github.sakurawald.module.initializer.fuji.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.sakurawald.core.auxiliary.minecraft.StackHelper;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.gui.PagedGui;
import io.github.sakurawald.core.gui.inspection.CommandDescriptorGui;
import io.github.sakurawald.core.manager.impl.module.ModuleManager;
import io.github.sakurawald.core.structure.descriptor.annotation.ColorBox;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ModuleDetailsInspectionGui extends PagedGui<GuiElementInterface> {

    public ModuleDetailsInspectionGui(@Nullable SimpleGui parent, ServerPlayerEntity player, Text prefixTitle, @NotNull List<GuiElementInterface> entities, int pageIndex) {
        super(parent, player, prefixTitle, entities, pageIndex);
    }

    @Override
    protected PagedGui<GuiElementInterface> make(@Nullable SimpleGui parent, ServerPlayerEntity player, Text title, @NotNull List<GuiElementInterface> entities, int pageIndex) {
        return new ModuleDetailsInspectionGui(parent, player, title, entities, pageIndex);
    }

    public static ModuleDetailsInspectionGui inspectModuleDetails(@Nullable SimpleGui parent, ServerPlayerEntity player, String modulePathString, boolean moduleEnableStatus) {
        /* Make the GUI. */
        List<GuiElementInterface> entities = new ArrayList<>();
        Text title = TextHelper.getTextByKey(player, "fuji.inspect.module_details.gui.title", modulePathString);
        ModuleDetailsInspectionGui moduleDetailsInspectionGui = new ModuleDetailsInspectionGui(parent, player, title, entities, 0);

        /* Search all types of objects of the module.  */
        SimpleGui trueParent = moduleDetailsInspectionGui.getGui();
        entities.addAll(searchModuleConfigurations(trueParent, player, modulePathString));
        entities.addAll(searchModuleCommands(trueParent, player, modulePathString));
        entities.addAll(searchModulePermissionsAndMetas(trueParent, player, modulePathString));
        entities.addAll(searchModulePlaceholders(trueParent, player, modulePathString));
        entities.addAll(searchModuleArgumentTypes(trueParent, player, modulePathString));

        /* Attach color blocks of the module. */
        attachColorBlocks(player, entities, modulePathString);

        /* Fill items. */
        if (!moduleEnableStatus) {
            placeModuleDisabledTipsItem(player, moduleDetailsInspectionGui);
        }

        return moduleDetailsInspectionGui;
    }

    private static void placeModuleDisabledTipsItem(ServerPlayerEntity player, ModuleDetailsInspectionGui gui) {
        /* Make the item. */
        GuiElementBuilder builder = new GuiElementBuilder()
            .setItem(Items.RED_STAINED_GLASS_PANE)
            .setName(TextHelper.getTextByKey(player, "module.status.disabled.gui.name"))
            .setLore(TextHelper.getTextListByKey(player, "module.status.disabled.gui.lore"));

        /* Place it on empty slots. */
        for (int i = 0; i < gui.getSize(); i++) {
            GuiElementInterface slot = gui.getSlot(i);
            if (slot == null
                || slot.getItemStack() == null
                || slot.getItemStack().isEmpty()) {
                gui.setSlot(i, builder.build());
            }
        }

    }


    private static void attachColorBlocks(ServerPlayerEntity player, List<GuiElementInterface> entities, String modulePathString) {
        /* Get the module initializer class. */
        Class<? extends ModuleInitializer> moduleInitializerClass = ModuleManager.MODULE_INITIALIZER_CLASS_BY_MODULE_PATH_STRING
            .get(modulePathString);
        if (moduleInitializerClass == null) return;

        /* Iterate the color boxes. */
        ColorBox[] boxes = moduleInitializerClass
            .getDeclaredAnnotationsByType(ColorBox.class);

        for (ColorBox box : boxes) {
            GuiElementBuilder colorboxElementBuilder = new GuiElementBuilder();

            ColorBox.ColorBlockTypes color = box.color();
            String documentString = box.value();
            colorboxElementBuilder
                .setName(TextHelper.getTextByKey(player, color.toLanguageKey()))
                .setItem(color.toItem())
                .setLore(TextHelper.getDocumentTextList(player, documentString))
                .setCallback(() -> sendColorBoxMessage(player, documentString));

            entities.add(colorboxElementBuilder.build());
        }

    }

    private static void sendColorBoxMessage(ServerPlayerEntity player, String documentString) {
        TextHelper.getDocumentTextList(player, documentString)
            .forEach(player::sendMessage);
    }

    private static List<GuiElementInterface> searchModuleArgumentTypes(@Nullable SimpleGui parent, ServerPlayerEntity player, String modulePathString) {
        return ArgumentTypeInspectionGui
            .inspectAll(parent, player)
            .skipCurrentGuiAndSearch(it -> it.getFromModule().equals(modulePathString))
            .toGuiElements();
    }

    private static List<GuiElementInterface> searchModuleConfigurations(@Nullable SimpleGui parent, ServerPlayerEntity player, String modulePathString) {
        return ConfigurationHandlerInspectionGui
            .inspectAll(parent, player)
            .skipCurrentGuiAndSearch(it -> it.getFromModule().equals(modulePathString))
            .toGuiElements();
    }

    private static List<GuiElementInterface> searchModulePlaceholders(@Nullable SimpleGui parent, ServerPlayerEntity player, String modulePathString) {
        return PlaceholderDescriptorInspectionGui
            .inspectAll(parent, player)
            .skipCurrentGuiAndSearch(it -> it.getSourceModule().equals(modulePathString))
            .toGuiElements();
    }

    private static List<GuiElementInterface> searchModuleCommands(SimpleGui parent, ServerPlayerEntity player, String modulePathString) {
        return CommandDescriptorGui
            .inspectAll(parent, player)
            .skipCurrentGuiAndSearch(it -> it.getSourceModule().equals(modulePathString))
            .toGuiElements();
    }

    private static List<GuiElementInterface> searchModulePermissionsAndMetas(@Nullable SimpleGui parent, ServerPlayerEntity player, String modulePathString) {
        return PermissionsAndMetasInspectionGui
            .inspectAll(parent, player)
            .skipCurrentGuiAndSearch(it -> it.getSourceModule().equals(modulePathString))
            .toGuiElements();
    }

    @Override
    protected GuiElementInterface toGuiElement(GuiElementInterface entity) {
        return entity;
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override
    protected List<GuiElementInterface> filter(String keyword) {
        return getEntities()
            .stream()
            .filter(it -> {
                ItemStack itemStack = it.getItemStack();
                /* Filter by item name. */
                String itemName = TextHelper.visitString(itemStack.getName());
                if (itemName.toLowerCase().contains(keyword.toLowerCase())) return true;

                /* Filter by item lore. */
                boolean matched = StackHelper.getLore(itemStack)
                    .stream()
                    .anyMatch(text -> TextHelper.visitString(text)
                        .toLowerCase()
                        .contains(keyword.toLowerCase()));
                if (matched) return true;

                return false;
            })
            .toList();
    }

}

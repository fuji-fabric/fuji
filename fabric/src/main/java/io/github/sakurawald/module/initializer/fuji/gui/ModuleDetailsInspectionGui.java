package io.github.sakurawald.module.initializer.fuji.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.sakurawald.core.auxiliary.minecraft.GuiHelper;
import io.github.sakurawald.core.auxiliary.minecraft.StackHelper;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.config.Configs;
import io.github.sakurawald.core.gui.PagedGui;
import io.github.sakurawald.core.gui.inspection.CommandsInspectionGui;
import io.github.sakurawald.core.manager.impl.module.ModuleManager;
import io.github.sakurawald.core.structure.descriptor.annotation.ColorBox;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import io.github.sakurawald.module.initializer.fuji.FujiInitializer;
import lombok.NonNull;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ModuleDetailsInspectionGui extends PagedGui<GuiElementInterface> {

    public ModuleDetailsInspectionGui(@Nullable SimpleGui parent, ServerPlayerEntity player, Text prefixTitle, @NotNull List<GuiElementInterface> entities, int pageIndex) {
        super(parent, player, prefixTitle, entities, pageIndex);
    }

    public static void attachThingsForCore(ServerPlayerEntity player, ModuleDetailsInspectionGui gui, List<GuiElementInterface> entities, String modulePathString) {
        /* Only attach things for core module. */
        if (!modulePathString.equals(ModuleManager.CORE_MODULE_NAME)) return;

        /* Place about button. */
        GuiElementBuilder aboutButton = new GuiElementBuilder()
            .setItem(Items.NETHER_STAR)
            .setName(TextHelper.getTextByKey(player, "about"))
            .setCallback(() -> AboutGui.make(gui.getBackendGui(), player).open());
        entities.add(aboutButton.build());

        /* Place user guide button. */
        GuiElementBuilder userGuideButton = new GuiElementBuilder()
            .setItem(Items.BOOK)
            .setName(TextHelper.getTextByKey(player, "user_guide"))
            .glow()
            .setCallback(() -> {
                gui.closeWithoutOpenParentGui();
                FujiInitializer.$userGuide(player);
            });
        entities.add( userGuideButton.build());

        /* Place reload button. */
        GuiElementBuilder reloadButton = new GuiElementBuilder()
            .setItem(Items.TARGET)
            .setName(TextHelper.getTextByKey(player, "reload.gui.name"))
            .setCallback(() -> {
                gui.closeWithoutOpenParentGui();
                FujiInitializer.$reload(player);
            });
        entities.add(reloadButton.build());

        /* Place debug button. */
        var debugConfig = Configs.mainControlConfig.model().core.debug;
        GuiElementBuilder debugButton = new GuiElementBuilder()
            .setItem(debugConfig.log_debug_messages ? Items.GREEN_BANNER : Items.RED_BANNER)
            .setName(TextHelper.getTextByKey(player, "debug"))
            .setCallback(() -> {
                gui.closeWithoutOpenParentGui();
                FujiInitializer.$debug(player);
            });
        entities.add(debugButton.build());

        /* Fill the first line. */
        // NOTE: yy4p
        entities.add(GuiHelper.makeSlotPlaceholder());
        entities.add(GuiHelper.makeSlotPlaceholder());
        entities.add(GuiHelper.makeSlotPlaceholder());
        entities.add(GuiHelper.makeSlotPlaceholder());
        entities.add(GuiHelper.makeSlotPlaceholder());
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

        /* Attach things for core module. */
        attachThingsForCore(player, moduleDetailsInspectionGui, entities, modulePathString);

        /* Attach color blocks of the module. */
        attachColorBlocks(player, entities, modulePathString);

        /* Search all types of objects of the module.  */
        SimpleGui trueParent = moduleDetailsInspectionGui.getBackendGui();
        entities.addAll(searchModuleConfigurations(trueParent, player, modulePathString));
        entities.addAll(searchModuleCommands(trueParent, player, modulePathString));
        entities.addAll(searchModulePermissionsAndMetas(trueParent, player, modulePathString));
        entities.addAll(searchModulePlaceholders(trueParent, player, modulePathString));
        entities.addAll(searchModuleArgumentTypes(trueParent, player, modulePathString));

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
        getColorBoxes(modulePathString)
            .forEach(colorBox -> {
                GuiElementBuilder colorboxElementBuilder = new GuiElementBuilder();

                ColorBox.ColorBlockTypes color = colorBox.color();
                String documentString = colorBox.value();
                List<Text> colorBoxTextList = TextHelper.getDocumentTextList(player, documentString);
                colorboxElementBuilder
                    .setName(TextHelper.getTextByKey(player, color.toLanguageKey()))
                    .setItem(color.toItem())
                    .setLore(colorBoxTextList)
                    .setCallback(() -> sendColorBoxMessage(player, colorBoxTextList));

                entities.add(colorboxElementBuilder.build());

            });
    }

    public static @NonNull List<ColorBox> getColorBoxes(String modulePathString) {
        /* Get the module initializer class. */
        Class<? extends ModuleInitializer> moduleInitializerClass = ModuleManager.MODULE_INITIALIZER_CLASS_BY_MODULE_PATH_STRING
            .get(modulePathString);
        if (moduleInitializerClass == null) return List.of();

        /* Iterate the color boxes. */
        ColorBox[] boxes = moduleInitializerClass
            .getDeclaredAnnotationsByType(ColorBox.class);

        return Arrays.asList(boxes);
    }

    private static void sendColorBoxMessage(ServerPlayerEntity player, List<Text> colorBoxTestList) {
        colorBoxTestList.forEach(player::sendMessage);
        player.sendMessage(TextHelper.TEXT_EMPTY);
    }

    private static List<GuiElementInterface> searchModuleArgumentTypes(@Nullable SimpleGui parent, ServerPlayerEntity player, String modulePathString) {
        return ArgumentTypesInspectionGui
            .inspectAll(parent, player)
            .skipCurrentGuiAndSearch(it -> it.getFromModule().equals(modulePathString))
            .toGuiElements();
    }

    private static List<GuiElementInterface> searchModuleConfigurations(@Nullable SimpleGui parent, ServerPlayerEntity player, String modulePathString) {
        return ConfigurationsInspectionGui
            .inspectAll(parent, player)
            .skipCurrentGuiAndSearch(it -> it.getFromModule().equals(modulePathString))
            .toGuiElements();
    }

    private static List<GuiElementInterface> searchModulePlaceholders(@Nullable SimpleGui parent, ServerPlayerEntity player, String modulePathString) {
        return PlaceholdersInspectionGui
            .inspectAll(parent, player)
            .skipCurrentGuiAndSearch(it -> it.getSourceModule().equals(modulePathString))
            .toGuiElements();
    }

    private static List<GuiElementInterface> searchModuleCommands(SimpleGui parent, ServerPlayerEntity player, String modulePathString) {
        return CommandsInspectionGui
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

    @Override
    protected boolean filterEntity(GuiElementInterface entity, String keyword) {
        return StackHelper.filterItemStack(entity.getItemStack(), keyword);
    }

}

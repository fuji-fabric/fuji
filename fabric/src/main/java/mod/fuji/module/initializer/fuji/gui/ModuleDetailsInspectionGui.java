package mod.fuji.module.initializer.fuji.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import mod.fuji.core.auxiliary.minecraft.GuiHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.config.Configs;
import mod.fuji.core.document.auxiliary.DocumentUtil;
import mod.fuji.core.gui.component.gui.PagedGui;
import mod.fuji.core.document.gui.CommandsInspectionGui;
import mod.fuji.core.document.annotation.ColorBox;
import mod.fuji.core.module.ModulePathResolver;
import mod.fuji.module.initializer.fuji.FujiInitializer;

import net.minecraft.world.item.Items;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ModuleDetailsInspectionGui extends PagedGui<GuiElementInterface> {

    public ModuleDetailsInspectionGui(@Nullable SimpleGui parent, ServerPlayer player, Component prefixTitle, @NotNull List<GuiElementInterface> entities, int pageIndex) {
        super(parent, player, prefixTitle, entities, pageIndex);
        this.streamMessageIntoToast = false;
    }

    public static void attachThingsForCore(ServerPlayer player, ModuleDetailsInspectionGui gui, List<GuiElementInterface> entities, String modulePathString) {
        /* Only attach things for core module. */
        if (!modulePathString.equals(ModulePathResolver.CORE_MODULE_PATH_STRING)) return;

        /* Place about button. */
        GuiElementBuilder aboutButton = GuiHelper.Button
            .makeModIconButton()
            .setName(TextHelper.getTextByKey(player, "about"))
            .setLore(List.of(TextHelper.getTextByKey(player, "prompt.click.see_inside")))
            .setCallback(() -> AboutGui.make(gui.getBackendGui(), player).open());
        entities.add(aboutButton.build());

        /* Place user guide button. */
        GuiElementBuilder userGuideButton = new GuiElementBuilder()
            .setItem(Items.BOOK)
            .setName(TextHelper.getTextByKey(player, "user_guide"))
            .setLore(List.of(TextHelper.getTextByKey(player, "prompt.click.see_it.any")))
            .glow()
            .setCallback(() -> {
                gui.closeWithoutOpenParentGui();
                FujiInitializer.$userGuide(player.createCommandSourceStack());
            });
        entities.add( userGuideButton.build());

        /* Place languages button. */
        GuiElementBuilder languagesButton = new GuiElementBuilder()
            .setItem(Items.CARTOGRAPHY_TABLE)
            .setName(TextHelper.getTextByKey(player, "language"))
            .setLore(List.of(TextHelper.getTextByKey(player, "prompt.click.see_inside")))
            .setCallback(() -> {
                LanguagesInspectionGui
                    .inspectAll(gui.getBackendGui(), player)
                    .open();
            });
        entities.add( languagesButton.build());

        /* Place reload button. */
        GuiElementBuilder reloadButton = new GuiElementBuilder()
            .setItem(Items.TARGET)
            .setName(TextHelper.getTextByKey(player, "reload.gui.name"))
            .setLore(List.of(TextHelper.getTextByKey(player, "prompt.click.apply_it")))
            .setCallback(() -> {
                gui.closeWithoutOpenParentGui();
                FujiInitializer.$reload(player.createCommandSourceStack());
            });
        entities.add(reloadButton.build());

        /* Place debug button. */
        var debugConfig = Configs.MAIN_CONTROL_CONFIG.model().core.debug;
        GuiElementBuilder debugButton = new GuiElementBuilder()
            .setItem(debugConfig.print_debug_messages_in_console ? Items.GREEN_BANNER : Items.RED_BANNER)
            .setName(TextHelper.getTextByKey(player, "debug"))
            .setLore(List.of(TextHelper.getTextByKey(player, "prompt.click.apply_it")))
            .setCallback(() -> {
                gui.closeWithoutOpenParentGui();
                FujiInitializer.$debug(player.createCommandSourceStack());
            });
        entities.add(debugButton.build());

        /* Fill the first line. */
        entities.add(GuiHelper.Button.makeSlotPlaceholderButton());
        entities.add(GuiHelper.Button.makeSlotPlaceholderButton());
        entities.add(GuiHelper.Button.makeSlotPlaceholderButton());
        entities.add(GuiHelper.Button.makeSlotPlaceholderButton());
    }

    @Override
    protected @NotNull PagedGui<GuiElementInterface> makePage(@Nullable SimpleGui parent, @NotNull ServerPlayer player, Component title, @NotNull List<GuiElementInterface> entities, int pageIndex) {
        return new ModuleDetailsInspectionGui(parent, player, title, entities, pageIndex);
    }

    public static ModuleDetailsInspectionGui inspectModuleDetails(@Nullable SimpleGui parent, @NotNull ServerPlayer player, @NotNull String modulePathString, boolean moduleEnableStatus) {
        /* Make the GUI. */
        List<GuiElementInterface> entities = new ArrayList<>();
        Component title = TextHelper.getTextByKey(player, "fuji.inspect.module_details.gui.title", modulePathString);
        ModuleDetailsInspectionGui moduleDetailsInspectionGui = new ModuleDetailsInspectionGui(parent, player, title, entities, 0);

        /* Attach things for core module. */
        attachThingsForCore(player, moduleDetailsInspectionGui, entities, modulePathString);

        /* Attach color blocks of the module. */
        attachColorBoxes(player, entities, modulePathString);

        /* Search all types of objects of the module.  */
        SimpleGui trueParent = moduleDetailsInspectionGui.getBackendGui();
        entities.addAll(searchModuleConfigurations(trueParent, player, modulePathString));
        entities.addAll(searchModuleCommands(trueParent, player, modulePathString));
        entities.addAll(searchModulePermissionsAndMetas(trueParent, player, modulePathString));
        entities.addAll(searchModulePlaceholders(trueParent, player, modulePathString));
        entities.addAll(searchModuleJobs(trueParent, player, modulePathString));
        entities.addAll(searchModuleArgumentTypes(trueParent, player, modulePathString));

        /* Fill items. */
        if (!moduleEnableStatus) {
            placeModuleDisabledTipsItem(player, moduleDetailsInspectionGui);
        }

        return moduleDetailsInspectionGui;
    }

    private static List<GuiElementInterface> searchModuleJobs(SimpleGui parent, ServerPlayer player, String modulePathString) {
        return JobsInspectionGui
            .inspectAll(parent, player)
            .skipCurrentGuiAndSearch(it -> it.getSourceModule().equals(modulePathString))
            .toGuiElements();
    }

    private static void placeModuleDisabledTipsItem(ServerPlayer player, ModuleDetailsInspectionGui gui) {
        /* Make the item. */
        GuiElementBuilder builder = new GuiElementBuilder()
            .setItem(Items.RED_STAINED_GLASS_PANE)
            .setName(TextHelper.getTextByKey(player, "module.status.disabled.gui.name"))
            .setLore(TextHelper.getTextListByKey(player, "module.status.disabled.gui.lore"));

        /* Place it on empty slots. */
        GuiHelper.Placer.fillEmptySlots(gui, builder);
   }

    private static void attachColorBoxes(ServerPlayer player, List<GuiElementInterface> entities, String modulePathString) {
        DocumentUtil.getColorBoxes(modulePathString)
            .forEach(colorBox -> {
                GuiElementBuilder colorboxElementBuilder = new GuiElementBuilder();

                ColorBox.ColorBoxTypes color = colorBox.color();
                Component colorBoxName = TextHelper.getTextByKey(player, color.toLanguageKey());

                String documentString = DocumentUtil.getColorBoxString(player, colorBox);
                List<Component> colorBoxTextList = TextHelper.getDocumentTextList(player, documentString);
                colorboxElementBuilder
                    .setName(colorBoxName)
                    .setItem(color.toItem())
                    .setLore(colorBoxTextList)
                    .setCallback(() -> sendColorBoxMessage(player, colorBoxName, colorBoxTextList));

                entities.add(colorboxElementBuilder.build());

            });
    }

    private static void sendColorBoxMessage(ServerPlayer player, Component colorBoxName, List<Component> colorBoxTestList) {
        TextHelper.sendMessageByText(player, colorBoxName);
        colorBoxTestList.forEach(text -> TextHelper.sendMessageByText(player, text));
        TextHelper.sendMessageByText(player, TextHelper.TEXT_EMPTY);
        TextHelper.sendMessageByText(player, TextHelper.TEXT_EMPTY);
    }

    private static List<GuiElementInterface> searchModuleArgumentTypes(@Nullable SimpleGui parent, ServerPlayer player, String modulePathString) {
        return ArgumentTypesInspectionGui
            .inspectAll(parent, player)
            .skipCurrentGuiAndSearch(it -> it.getSourceModule().equals(modulePathString))
            .toGuiElements();
    }

    private static List<GuiElementInterface> searchModuleConfigurations(@Nullable SimpleGui parent, ServerPlayer player, String modulePathString) {
        return ConfigurationsInspectionGui
            .inspectAll(parent, player)
            .skipCurrentGuiAndSearch(it -> it.getSourceModule().equals(modulePathString))
            .toGuiElements();
    }

    private static List<GuiElementInterface> searchModulePlaceholders(@Nullable SimpleGui parent, ServerPlayer player, String modulePathString) {
        return PlaceholdersInspectionGui
            .inspectAll(parent, player)
            .skipCurrentGuiAndSearch(it -> it.getSourceModule().equals(modulePathString))
            .toGuiElements();
    }

    private static List<GuiElementInterface> searchModuleCommands(SimpleGui parent, ServerPlayer player, String modulePathString) {
        return CommandsInspectionGui
            .inspectAll(parent, player)
            .skipCurrentGuiAndSearch(it -> it.getSourceModule().equals(modulePathString))
            .toGuiElements();
    }

    private static List<GuiElementInterface> searchModulePermissionsAndMetas(@Nullable SimpleGui parent, ServerPlayer player, String modulePathString) {
        return PermissionsAndMetasInspectionGui
            .inspectAll(parent, player)
            .skipCurrentGuiAndSearch(it -> it.getSourceModule().equals(modulePathString))
            .toGuiElements();
    }

    @Override
    protected @NotNull GuiElementInterface toGuiElement(@NotNull GuiElementInterface entity) {
        return entity;
    }

}

package io.github.sakurawald.module.initializer.fuji.gui;

import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.sakurawald.core.gui.PagedGui;
import io.github.sakurawald.core.gui.inspection.CommandDescriptorGui;
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

    public static ModuleDetailsInspectionGui inspectModuleDetails(@Nullable SimpleGui gui, ServerPlayerEntity player, String modulePathString) {

        /* Search all types of objects of the module.  */
        List<GuiElementInterface> entities = new ArrayList<>();
        entities.addAll(searchModuleCommands(gui, player, modulePathString));
        entities.addAll(searchModulePermissionsAndMetas(gui, player, modulePathString));

        /* Make the GUI. */
        Text title = Text.literal("module details gui");
        ModuleDetailsInspectionGui moduleDetailsInspectionGui = new ModuleDetailsInspectionGui(gui, player, title, entities, 0);

        return moduleDetailsInspectionGui;
    }

    private static List<GuiElementInterface> searchModuleCommands(SimpleGui parent, ServerPlayerEntity player, String modulePathString) {
        return CommandDescriptorGui
            .inspectAll(parent, player)
            .search(it -> it.getSourceModulePath().equals(modulePathString))
            .toGuiElements();
    }

    private static List<GuiElementInterface> searchModulePermissionsAndMetas(@Nullable SimpleGui parent, ServerPlayerEntity player, String modulePathString) {
        return PermissionsAndMetasInspectionGui
            .inspectAll(parent, player)
            .search(it -> it.getFromModule().equals(modulePathString))
            .toGuiElements();
    }

    @Override
    protected GuiElementInterface toGuiElement(GuiElementInterface entity) {
        return entity;
    }

    @Override
    protected List<GuiElementInterface> filter(String keyword) {
        return List.of();
    }
}

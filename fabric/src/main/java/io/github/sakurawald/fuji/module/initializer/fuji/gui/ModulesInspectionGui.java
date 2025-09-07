package io.github.sakurawald.fuji.module.initializer.fuji.gui;


import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.GuiHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.document.auxiliary.DocumentUtil;
import io.github.sakurawald.fuji.core.gui.component.gui.PagedGui;
import io.github.sakurawald.fuji.core.manager.impl.module.ModuleManager;
import io.github.sakurawald.fuji.core.manager.impl.module.ModulePathResolver;
import io.github.sakurawald.fuji.core.structure.Pair;
import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ModulesInspectionGui extends PagedGui<Pair<String, Boolean>> {

    public ModulesInspectionGui(SimpleGui parent, ServerPlayerEntity player, @NotNull List<Pair<String, Boolean>> entities, int pageIndex) {
        super(parent, player, TextHelper.getTextByKey(player, "fuji.inspect.modules.gui.title"), entities, pageIndex);

        int enabledModules = ModuleManager.getEnabledModulePaths().size();
        int allModules = ModuleManager.MODULE_ENABLE_STATUS.size();
        List<Text> lore = List.of(
            TextHelper.getTextByKey(player, "fuji.inspect.modules.gui.reimu.lore", enabledModules, allModules)
        );

        GuiHelper.Placer.setSlotInLastLine(this, 4, GuiHelper
            .Button
            .makeModIconButton()
            .setName(TextHelper.getTextByKey(player, "fuji.inspect.modules.gui.title"))
            .setLore(lore));
    }

    @Override
    protected PagedGui<Pair<String, Boolean>> makePage(@Nullable SimpleGui parent, @NotNull ServerPlayerEntity player, Text title, @NotNull List<Pair<String, Boolean>> entities, int pageIndex) {
        return new ModulesInspectionGui(parent, player, entities, pageIndex);
    }

    @SuppressWarnings("SequencedCollectionMethodCanBeUsed")
    public static ModulesInspectionGui inspectAll(SimpleGui parent, ServerPlayerEntity player) {
        List<Pair<String, Boolean>> entities = ModuleManager.MODULE_ENABLE_STATUS
            .entrySet()
            .stream()
            .map(it -> new Pair<>(ModulePathResolver.toModulePathString(it.getKey()), it.getValue()))
            .sorted(Comparator.comparing(Pair::getKey))
            .collect(Collectors.toList());

        /* Insert the core module as a dummy module. */
        entities.add(0, new Pair<>(ModulePathResolver.CORE_MODULE_PATH, true));

        return new ModulesInspectionGui(parent, player, entities, 0);
    }

    @Override
    protected @NotNull GuiElementInterface toGuiElement(@NotNull Pair<String, Boolean> entity) {
        List<Text> lore = new ArrayList<>();

        /* Attach module enable status. */
        boolean moduleEnableStatus = entity.getValue();
        lore.add(TextHelper.getTextByKey(getPlayer(), "module.enable.status", moduleEnableStatus));

        /* Attach color boxes amount. */
        String modulePathString = entity.getKey();
        List<ColorBox> colorBoxes = ModuleDetailsInspectionGui.getColorBoxes(modulePathString);

        /* Attach click prompt. */
        if (moduleEnableStatus
            || !colorBoxes.isEmpty()) {
            lore.addAll(TextHelper.getTextListByKey(getPlayer(), "prompt.click.see_inside"));
        }

        /* Attach @Document information above module initializer. */
        Class<? extends ModuleInitializer> moduleInitializerClass = ModuleManager.MODULE_INITIALIZER_CLASS_BY_MODULE_PATH_STRING.getOrDefault(modulePathString, null);
        if (moduleInitializerClass != null) {
            String classDocument = DocumentUtil.getClassDocumentString(getPlayer(), moduleInitializerClass);
            if (classDocument != null) {
                lore.add(TextHelper.TEXT_EMPTY);
                lore.addAll(TextHelper.getDocumentTextList(getPlayer(), classDocument ));
            }
        }

        /* Make the element. */
        Item itemMaterial = getItemMaterial(entity);
        Text itemName = Text.literal(modulePathString)
                            .formatted(Formatting.YELLOW);

        return new GuiElementBuilder()
            .setItem(itemMaterial)
            .setName(itemName)
            .setLore(lore)
            .setCallback(() -> openModuleDetailsInspectionGui(getBackendGui(), getPlayer(), modulePathString, moduleEnableStatus))
            .build();
    }

    private static Item getItemMaterial(Pair<String, Boolean> entity) {
        String modulePathString = entity.getKey();
        if (modulePathString.equals(ModulePathResolver.CORE_MODULE_PATH)) {
            return Items.TINTED_GLASS;
        }

        Boolean moduleEnableStatus = entity.getValue();
        return GuiHelper.Material.fromBooleanValue(moduleEnableStatus);
    }

    @SuppressWarnings("SameParameterValue")
    private void openModuleDetailsInspectionGui(@Nullable SimpleGui parent, ServerPlayerEntity player, String modulePathString, boolean moduleEnableStatus) {
        ModuleDetailsInspectionGui
            .inspectModuleDetails(parent, player, modulePathString, moduleEnableStatus)
            .open();
    }

}

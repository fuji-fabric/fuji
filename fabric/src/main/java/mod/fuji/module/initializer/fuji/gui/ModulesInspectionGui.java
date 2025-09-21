package mod.fuji.module.initializer.fuji.gui;


import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import mod.fuji.core.auxiliary.minecraft.GuiHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.document.auxiliary.DocumentUtil;
import mod.fuji.core.gui.component.gui.PagedGui;
import mod.fuji.core.manager.impl.module.ModuleLoadDeterminer;
import mod.fuji.core.manager.impl.module.ModuleManager;
import mod.fuji.core.manager.impl.module.ModulePathResolver;
import mod.fuji.core.structure.Pair;
import mod.fuji.core.document.annotation.ColorBox;
import mod.fuji.module.initializer.ModuleInitializer;
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

        int enabledModules = ModuleLoadDeterminer.getEnabledModulePaths().size();
        int allModules = ModuleLoadDeterminer.getDeclaredModulePaths().size();
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
        List<Pair<String, Boolean>> entities = ModuleLoadDeterminer.MODULE_ENABLE_STATUS
            .entrySet()
            .stream()
            .map(it -> new Pair<>(ModulePathResolver.toModulePathString(it.getKey()), it.getValue()))
            .sorted(Comparator.comparing(Pair::getKey))
            .collect(Collectors.toList());

        /* Insert the core module as a dummy module. */
        entities.add(0, new Pair<>(ModulePathResolver.CORE_MODULE_PATH_STRING, true));

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
        List<ColorBox> colorBoxes = DocumentUtil.getColorBoxes(modulePathString);

        /* Attach click prompt. */
        if (moduleEnableStatus
            || !colorBoxes.isEmpty()) {
            lore.addAll(TextHelper.getTextListByKey(getPlayer(), "prompt.click.see_inside"));
        }

        /* Attach @Document information above module initializer. */
        Class<? extends ModuleInitializer> moduleInitializerClass = ModuleManager.MODULE_INITIALIZER_CLASS_BY_MODULE_PATH_STRING.getOrDefault(modulePathString, null);
        if (moduleInitializerClass != null) {
            DocumentUtil
                .getClassDocumentString(getPlayer(), moduleInitializerClass)
                .ifPresent(moduleClassDocument -> {
                    lore.add(TextHelper.TEXT_EMPTY);
                    lore.addAll(TextHelper.getDocumentTextList(getPlayer(), moduleClassDocument ));
                });
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
        if (modulePathString.equals(ModulePathResolver.CORE_MODULE_PATH_STRING)) {
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

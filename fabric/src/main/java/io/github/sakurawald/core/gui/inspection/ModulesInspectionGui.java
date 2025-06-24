package io.github.sakurawald.core.gui.inspection;


import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.sakurawald.core.auxiliary.ReflectionUtil;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.command.structure.CommandDescriptor;
import io.github.sakurawald.core.gui.PagedGui;
import io.github.sakurawald.core.manager.impl.module.ModuleManager;
import io.github.sakurawald.core.structure.Pair;
import io.github.sakurawald.module.initializer.ModuleInitializer;
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

public class ModulesInspectionGui extends PagedGui<Pair<String, Boolean>> {

    public ModulesInspectionGui(ServerPlayerEntity player, @NotNull List<Pair<String, Boolean>> entities, int pageIndex) {
        super(null, player, TextHelper.getTextByKey(player, "fuji.inspect.modules.gui.title"), entities, pageIndex);
    }

    @Override
    protected PagedGui<Pair<String, Boolean>> make(@Nullable SimpleGui parent, ServerPlayerEntity player, Text title, @NotNull List<Pair<String, Boolean>> entities, int pageIndex) {
        return new ModulesInspectionGui(player, entities, pageIndex);
    }

    public static ModulesInspectionGui makeDefault(ServerPlayerEntity player) {
        List<Pair<String, Boolean>> list = ModuleManager.MODULE_ENABLE_STATUS
            .entrySet()
            .stream()
            .map(it -> new Pair<>(ModuleManager.joinModulePath(it.getKey()), it.getValue()))
            .sorted(Comparator.comparing(Pair::getKey))
            .toList();
        return new ModulesInspectionGui(player, list, 0);
    }

    @Override
    protected GuiElementInterface toGuiElement(Pair<String, Boolean> entity) {
        List<Text> lore = new ArrayList<>();

        /* Attach module enable status. */
        boolean moduleEnable = entity.getValue();
        lore.add(TextHelper.getTextByKey(getPlayer(), "module.enable.status", moduleEnable));

        /* Attach @Document information above module initializer. */
        String modulePathString = entity.getKey();
        Class<? extends ModuleInitializer> moduleInitializerClass = ModuleManager.MODULE_INITIALIZER_CLASS_BY_MODULE_PATH_STRING.getOrDefault(modulePathString, null);
        if (moduleInitializerClass != null) {
            String classDocument = getDocumentString(moduleInitializerClass);
            if (classDocument != null) {
                lore.add(TextHelper.TEXT_EMPTY);
                lore.addAll(TextHelper.getDocumentTextList(getPlayer(), classDocument ));
            }
        }

        /* Attach registered commands info. */
        PagedGui<CommandDescriptor> commandsRegisteredByThisModuleGUI = attachModuleCommands(modulePathString, lore);

        Item itemMaterial = moduleEnable ? Items.GREEN_STAINED_GLASS : Items.RED_STAINED_GLASS;
        Text itemName = Text.literal(modulePathString).formatted(Formatting.YELLOW);
        return new GuiElementBuilder()
            .setItem(itemMaterial)
            .setName(itemName)
            .setLore(lore)
            .setCallback(() -> onClickCommandDescriptor(modulePathString, commandsRegisteredByThisModuleGUI))
            .build();
    }

    private void onClickCommandDescriptor(String modulePathString, PagedGui<CommandDescriptor> commandsRegisteredByThisModuleGUI) {

       /* Click to open the registered commands. */
       commandsRegisteredByThisModuleGUI.open();

    }

    private PagedGui<CommandDescriptor> attachModuleCommands(String modulePathString, List<Text> lore) {
        PagedGui<CommandDescriptor> commandsRegisteredByThisModuleGUI = CommandDescriptorGui
            .makeDefault(getGui(), getPlayer())
            .search(it -> it.getSourceModulePath().equals(modulePathString));


        lore.add(TextHelper.TEXT_EMPTY);
        int registeredCommandsCount = commandsRegisteredByThisModuleGUI.getEntities().size();
        lore.add(TextHelper.getTextByKey(getPlayer(), "module.registered_commands", registeredCommandsCount));
        if (registeredCommandsCount != 0) {
            lore.add(TextHelper.getTextByKey(getPlayer(), "prompt.click.see_it.left_click"));
        }

        return commandsRegisteredByThisModuleGUI;
    }

    private static @Nullable String getDocumentString(Class<? extends ModuleInitializer> moduleInitializerClass) {
        @Nullable String classDocument = ReflectionUtil.getClassDocument(moduleInitializerClass);
        return classDocument;
    }

    @Override
    protected List<Pair<String, Boolean>> filter(String keyword) {
        return getEntities().stream()
            .filter(it -> it.getKey().contains(keyword)
                || it.getValue().toString().contains(keyword)).toList();
    }
}

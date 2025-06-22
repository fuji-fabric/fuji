package io.github.sakurawald.module.initializer.fuji.gui;


import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.sakurawald.core.auxiliary.ReflectionUtil;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
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
import java.util.List;

public class ModulesGui extends PagedGui<Pair<String, Boolean>> {

    public ModulesGui(ServerPlayerEntity player, @NotNull List<Pair<String, Boolean>> entities, int pageIndex) {
        super(null, player, TextHelper.getTextByKey(player, "fuji.inspect.modules.gui.title"), entities, pageIndex);
    }

    @Override
    protected PagedGui<Pair<String, Boolean>> make(@Nullable SimpleGui parent, ServerPlayerEntity player, Text title, @NotNull List<Pair<String, Boolean>> entities, int pageIndex) {
        return new ModulesGui(player, entities, pageIndex);
    }

    @Override
    protected GuiElementInterface toGuiElement(Pair<String, Boolean> entity) {
        List<Text> lore = new ArrayList<>();

        /* Add module enable status. */
        boolean moduleEnable = entity.getValue();
        lore.add(TextHelper.getTextByKey(getPlayer(), "module.enable.status", moduleEnable));

        /* Extract the @Document from class annotation above module initializer. */
        String modulePathString = entity.getKey();
        ModuleInitializer moduleInitializer = ModuleManager.MODULE_INITIALIZER_BY_MODULE_PATH_STRING.getOrDefault(modulePathString, null);
        if (moduleInitializer != null) {
            @Nullable String classDocument = ReflectionUtil.getClassDocument(moduleInitializer.getClass());
            if (classDocument != null) {
                lore.add(TextHelper.TEXT_EMPTY);
                lore.addAll(TextHelper.getTextListByValue(getPlayer(), classDocument));
            }
        }

        Item itemMaterial = moduleEnable ? Items.GREEN_STAINED_GLASS : Items.RED_STAINED_GLASS;
        Text itemName = Text.literal(modulePathString).formatted(moduleEnable ? Formatting.GREEN : Formatting.RED);
        return new GuiElementBuilder()
            .setItem(itemMaterial)
            .setName(itemName)
            .setLore(lore)
            .build();
    }

    @Override
    protected List<Pair<String, Boolean>> filter(String keyword) {
        return getEntities().stream()
            .filter(it -> it.getKey().contains(keyword)
                || it.getValue().toString().contains(keyword)).toList();
    }
}

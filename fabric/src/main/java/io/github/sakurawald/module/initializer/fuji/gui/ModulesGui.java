package io.github.sakurawald.module.initializer.fuji.gui;


import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.gui.PagedGui;
import io.github.sakurawald.core.structure.Pair;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        return new GuiElementBuilder()
            .setName(Text.literal(entity.getKey()))
            .setItem(entity.getValue() ? Items.GREEN_STAINED_GLASS : Items.RED_STAINED_GLASS)
            .build();
    }

    @Override
    protected List<Pair<String, Boolean>> filter(String keyword) {
        return getEntities().stream()
            .filter(it -> it.getKey().contains(keyword)
                || it.getValue().toString().contains(keyword)).toList();
    }
}

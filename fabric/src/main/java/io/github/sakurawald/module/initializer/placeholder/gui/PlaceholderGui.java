package io.github.sakurawald.module.initializer.placeholder.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.gui.PagedGui;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PlaceholderGui extends PagedGui<Identifier> {

    public PlaceholderGui(ServerPlayerEntity player, @NotNull List<Identifier> entities, int pageIndex) {
        super(null, player, TextHelper.getTextByKey(player, "placeholder.list.gui.title"), entities, pageIndex);
    }

    @Override
    protected PagedGui<Identifier> make(@Nullable SimpleGui parent, ServerPlayerEntity player, Text title, @NotNull List<Identifier> entities, int pageIndex) {
        return new PlaceholderGui(player, entities, pageIndex);
    }

    @Override
    protected GuiElementInterface toGuiElement(Identifier entity) {
        return new GuiElementBuilder()
            .setName(Text.literal(entity.toString()))
            .setItem(Items.NAME_TAG)
            .build();
    }

    @Override
    protected List<Identifier> filter(String keyword) {
        return getEntities().stream().filter(it -> it.toString().contains(keyword)).toList();
    }
}

package io.github.sakurawald.module.initializer.command_toolbox.warp.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.sakurawald.core.auxiliary.minecraft.RegistryHelper;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.gui.PagedGui;
import io.github.sakurawald.module.initializer.command_toolbox.warp.WarpInitializer;
import io.github.sakurawald.module.initializer.command_toolbox.warp.structure.WarpNode;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class WarpGui extends PagedGui<WarpNode> {

    public WarpGui(ServerPlayerEntity player, @NotNull List<WarpNode> entities, int pageIndex) {
        super(null, player, TextHelper.getTextByKey(player, "warp.gui.title"), entities, pageIndex);
    }

    @Override
    protected PagedGui<WarpNode> make(@Nullable SimpleGui parent, ServerPlayerEntity player, Text title, @NotNull List<WarpNode> entities, int pageIndex) {
        return new WarpGui(player, entities, pageIndex);
    }

    @Override
    protected GuiElementInterface toGuiElement(WarpNode entity) {
        return new GuiElementBuilder()
            .setName(TextHelper.getTextByValue(getPlayer(), entity.getName()))
            .setItem(RegistryHelper.ofItem(entity.getItem()))
            .setLore(new ArrayList<>() {
                {
                    entity.getLore().forEach(it -> this.add(TextHelper.getTextByValue(getPlayer(), it)));
                }
            })
            .setCallback(() -> {
                WarpInitializer.doWarp(entity, getPlayer());
                close();
            })
            .build();
    }

    @Override
    protected List<WarpNode> filter(String keyword) {
        return getEntities()
            .stream()
            .filter(it -> it.getName().contains(keyword)
                || it.getItem().contains(keyword)
                || it.getPosition().getLevel().contains(keyword))
            .toList();
    }
}

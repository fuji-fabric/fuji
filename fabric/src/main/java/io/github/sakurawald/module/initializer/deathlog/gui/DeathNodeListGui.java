package io.github.sakurawald.module.initializer.deathlog.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.gui.PagedGui;
import io.github.sakurawald.module.initializer.deathlog.structure.DeathNode;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DeathNodeListGui extends PagedGui<DeathNode> {

    private final String deadPlayerName;

    public DeathNodeListGui(@Nullable SimpleGui parent, ServerPlayerEntity player, String deadPlayerName, @NotNull List<DeathNode> entities, int pageIndex) {
        super(parent, player, TextHelper.getTextByKey(player, "deathlog.death_node.list.gui.title", deadPlayerName), entities, pageIndex);
        this.deadPlayerName = deadPlayerName;
    }

    @Override
    protected PagedGui<DeathNode> make(@Nullable SimpleGui parent, ServerPlayerEntity player, Text title, @NotNull List<DeathNode> entities, int pageIndex) {
        return new DeathNodeListGui(parent, player, this.deadPlayerName, entities, pageIndex);
    }

    @Override
    protected GuiElementInterface toGuiElement(DeathNode entity) {
        List<Text> lore = new ArrayList<>();
        ServerPlayerEntity player = getPlayer();
        lore.add(TextHelper.getTextByKey(player, "deathlog.view.time", entity.time));
        lore.add(TextHelper.getTextByKey(player, "deathlog.view.dimension", entity.dimension));
        lore.add(TextHelper.getTextByKey(player, "deathlog.view.coordinate", entity.x, entity.y, entity.z));
        lore.add(TextHelper.getTextByKey(player, "deathlog.view.reason", entity.reason));

        return new GuiElementBuilder()
            .setItem(Items.CHEST)
            .setName(Text.of(entity.time))
            .setLore(lore)
            .setCallback(() -> openDeathNodeDisplayGui(entity))
            .build();
    }

    private void openDeathNodeDisplayGui(DeathNode entity) {
        new DeathNodeDisplayGuiFactory(getGui(), entity)
            .build(getPlayer())
            .open();
    }

    @Override
    protected List<DeathNode> filter(String keyword) {
        return getEntities()
            .stream()
            .filter(it -> it.dimension.contains(keyword)
            || it.time.contains(keyword)
            || it.reason.toLowerCase().contains(keyword.toLowerCase())
            || it.main.toString().toLowerCase().contains(keyword.toLowerCase())
            || it.armor.toString().toLowerCase().contains(keyword.toLowerCase())
            || it.offhand.toString().toLowerCase().contains(keyword.toLowerCase()))
            .toList();
    }
}

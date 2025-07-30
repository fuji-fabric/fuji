package io.github.sakurawald.fuji.module.initializer.deathlog.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.sakurawald.fuji.core.auxiliary.StringUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.gui.impl.gui.PagedGui;
import io.github.sakurawald.fuji.module.initializer.deathlog.structure.DeathNode;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        return new GuiElementBuilder()
            .setItem(Items.CHEST)
            .setName(Text.of(entity.time))
            .setLore(entity.getLore(getPlayer()))
            .setCallback(() -> openDeathNodeDisplayGui(entity))
            .build();
    }

    private void openDeathNodeDisplayGui(DeathNode entity) {
        new DeathNodeDisplayGuiFactory(getBackendGui(), entity)
            .build(getPlayer())
            .open();
    }

    @Override
    protected boolean filterEntity(DeathNode entity, String keyword) {
        // NOTE: Make it possible to search a specific item in death node.
        return entity.dimension.contains(keyword)
            || entity.time.contains(keyword)
            || StringUtil.containsIgnoreCase(entity.reason, keyword)
            || StringUtil.containsIgnoreCase(entity.main.toString(), keyword)
            || StringUtil.containsIgnoreCase(entity.armor.toString(), keyword)
            || StringUtil.containsIgnoreCase(entity.offhand.toString(), keyword);
    }
}

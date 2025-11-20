package mod.fuji.module.initializer.deathlog.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import mod.fuji.core.auxiliary.StringUtil;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.gui.component.gui.PagedGui;
import mod.fuji.module.initializer.deathlog.structure.DeathNode;
import net.minecraft.world.item.Items;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DeathNodeListGui extends PagedGui<DeathNode> {

    private final String deadPlayerName;

    public DeathNodeListGui(@Nullable SimpleGui parent, ServerPlayer player, String deadPlayerName, @NotNull List<DeathNode> entities, int pageIndex) {
        super(parent, player, TextHelper.getTextByKey(player, "deathlog.death_node.list.gui.title", deadPlayerName), entities, pageIndex);
        this.deadPlayerName = deadPlayerName;
    }

    @Override
    protected @NotNull PagedGui<DeathNode> makePage(@Nullable SimpleGui parent, @NotNull ServerPlayer player, Component title, @NotNull List<DeathNode> entities, int pageIndex) {
        return new DeathNodeListGui(parent, player, this.deadPlayerName, entities, pageIndex);
    }

    @Override
    protected @NotNull GuiElementInterface toGuiElement(@NotNull DeathNode entity) {
        return new GuiElementBuilder()
            .setItem(Items.CHEST)
            .setName(Component.nullToEmpty(entity.time))
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
    protected boolean filterEntity(@NotNull DeathNode entity, @NotNull String keyword) {
        // NOTE: Make it possible to search a specific item in death node.
        return entity.dimension.contains(keyword)
            || entity.time.contains(keyword)
            || StringUtil.containsIgnoreCase(entity.reason, keyword)
            || StringUtil.containsIgnoreCase(entity.main.toString(), keyword)
            || StringUtil.containsIgnoreCase(entity.armor.toString(), keyword)
            || StringUtil.containsIgnoreCase(entity.offhand.toString(), keyword);
    }
}

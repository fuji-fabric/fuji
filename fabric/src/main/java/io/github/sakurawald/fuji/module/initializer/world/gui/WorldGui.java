package io.github.sakurawald.fuji.module.initializer.world.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.WorldHelper;
import io.github.sakurawald.fuji.core.gui.PagedGui;
import io.github.sakurawald.fuji.module.initializer.world.structure.DimensionNode;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WorldGui extends PagedGui<DimensionNode> {

    public WorldGui(ServerPlayerEntity player, @NotNull List<DimensionNode> entities, int pageIndex) {
        super(null, player, TextHelper.getTextByKey(player, "world.dimension.list.gui.title"), entities, pageIndex);
    }

    @Override
    protected PagedGui<DimensionNode> make(@Nullable SimpleGui parent, ServerPlayerEntity player, Text title, @NotNull List<DimensionNode> entities, int pageIndex) {
        return new WorldGui(player, entities, pageIndex);
    }

    @Override
    protected GuiElementInterface toGuiElement(DimensionNode entity) {
        return new GuiElementBuilder()
            .setName(Text.of(entity.getDimension()))
            .setItem(WorldHelper.getSensibleWorldItem(entity.getDimension_type()))
            .setLore(List.of(
                TextHelper.getTextByKey(getPlayer(), "world.dimension.loaded", entity.isDimensionLoaded())
                , TextHelper.getTextByKey(getPlayer(), "world.dimension.dimension_type", entity.getDimension_type())
                , TextHelper.getTextByKey(getPlayer(), "world.dimension.seed", entity.getSeed())
            ))
            .build();
    }

    @Override
    protected boolean filterEntity(DimensionNode entity, String keyword) {
        return entity.getDimension().contains(keyword)
                || entity.getDimension_type().contains(keyword);
    }
}

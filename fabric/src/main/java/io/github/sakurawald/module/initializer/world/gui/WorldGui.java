package io.github.sakurawald.module.initializer.world.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.gui.PagedGui;
import io.github.sakurawald.module.initializer.world.structure.DimensionNode;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.dimension.DimensionTypes;
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

    private Item computeItem(DimensionNode entity) {
        if (entity.getDimension_type().equals(DimensionTypes.OVERWORLD_ID.toString())) {
            return Items.GRASS_BLOCK;
        }

        if (entity.getDimension_type().equals(DimensionTypes.THE_END_ID.toString())) {
            return Items.END_STONE;
        }

        if (entity.getDimension_type().equals(DimensionTypes.THE_NETHER_ID.toString())) {
            return Items.NETHERRACK;
        }

        return Items.PAPER;
    }

    @Override
    protected GuiElementInterface toGuiElement(DimensionNode entity) {
        return new GuiElementBuilder()
            .setName(Text.of(entity.getDimension()))
            .setItem(this.computeItem(entity))
            .setLore(List.of(
                TextHelper.getTextByKey(getPlayer(), "world.dimension.loaded", entity.isDimensionLoaded())
                , TextHelper.getTextByKey(getPlayer(), "world.dimension.dimension_type", entity.getDimension_type())
                , TextHelper.getTextByKey(getPlayer(), "world.dimension.seed", entity.getSeed())
            ))
            .build();
    }

    @Override
    protected List<DimensionNode> filter(String keyword) {
        return getEntities().stream()
            .filter(it -> it.getDimension().contains(keyword)
                || it.getDimension_type().contains(keyword))
            .toList();
    }
}

package mod.fuji.module.initializer.placeholder.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.gui.component.gui.PagedGui;
import mod.fuji.core.structure.IdentifierIR;
import net.minecraft.world.item.Items;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PlaceholderGui extends PagedGui<IdentifierIR> {

    public PlaceholderGui(ServerPlayer player, @NotNull List<IdentifierIR> entities, int pageIndex) {
        super(null, player, TextHelper.getTextByKey(player, "placeholder.list.gui.title"), entities, pageIndex);
    }

    @Override
    protected @NotNull PagedGui<IdentifierIR> makePage(@Nullable SimpleGui parent, @NotNull ServerPlayer player, Component title, @NotNull List<IdentifierIR> entities, int pageIndex) {
        return new PlaceholderGui(player, entities, pageIndex);
    }

    @Override
    protected @NotNull GuiElementInterface toGuiElement(@NotNull IdentifierIR entity) {
        return new GuiElementBuilder()
            .setName(Component.literal(entity.toString()))
            .setItem(Items.NAME_TAG)
            .build();
    }

}

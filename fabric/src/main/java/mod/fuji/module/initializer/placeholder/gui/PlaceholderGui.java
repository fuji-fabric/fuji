package mod.fuji.module.initializer.placeholder.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.gui.component.gui.PagedGui;
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
    protected PagedGui<Identifier> makePage(@Nullable SimpleGui parent, @NotNull ServerPlayerEntity player, Text title, @NotNull List<Identifier> entities, int pageIndex) {
        return new PlaceholderGui(player, entities, pageIndex);
    }

    @Override
    protected @NotNull GuiElementInterface toGuiElement(@NotNull Identifier entity) {
        return new GuiElementBuilder()
            .setName(Text.literal(entity.toString()))
            .setItem(Items.NAME_TAG)
            .build();
    }

}

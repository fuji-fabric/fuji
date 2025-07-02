package io.github.sakurawald.fuji.module.initializer.warning.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.GuiHelper;
import io.github.sakurawald.fuji.core.gui.impl.gui.PagedGui;
import java.util.List;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WarningGui extends PagedGui<String> {

    public WarningGui(@Nullable SimpleGui parent, @NotNull ServerPlayerEntity player, @NotNull List<String> entities, int pageIndex) {
        super(parent, player, Text.literal("warnings for all players"), entities, pageIndex);
    }

    @Override
    protected PagedGui<String> make(@Nullable SimpleGui parent, ServerPlayerEntity player, Text title, @NotNull List<String> entities, int pageIndex) {
        return new WarningGui(parent, player, entities, pageIndex);
    }

    @Override
    protected GuiElementInterface toGuiElement(String entity) {
        GuiElementBuilder builder = new GuiElementBuilder();

        GuiHelper.setPlayerHeadTexture(builder, entity);

        builder
            .setName(Text.literal(entity));


        return builder.build();
    }

    @Override
    protected boolean filterEntity(String entity, String keyword) {
        return false;
    }
}

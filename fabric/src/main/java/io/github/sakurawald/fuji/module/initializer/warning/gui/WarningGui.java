package io.github.sakurawald.fuji.module.initializer.warning.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.GuiHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.gui.component.gui.PagedGui;
import java.util.List;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WarningGui extends PagedGui<String> {

    public WarningGui(@Nullable SimpleGui parent, @NotNull ServerPlayerEntity player, @NotNull List<String> entities, int pageIndex) {
        super(parent, player, TextHelper.getTextByKey(player, "warning.gui.title"), entities, pageIndex);
    }

    @Override
    protected PagedGui<String> makePage(@Nullable SimpleGui parent, @NotNull ServerPlayerEntity player, Text title, @NotNull List<String> entities, int pageIndex) {
        return new WarningGui(parent, player, entities, pageIndex);
    }

    @Override
    protected void drawPagedGui() {
        super.drawPagedGui();

        GuiHelper.PlayerSkull.fillPlayerHeadTextures(this);
    }

    @Override
    protected @NotNull GuiElementInterface toGuiElement(@NotNull String entity) {
        GuiElementBuilder builder = GuiHelper.Button.makeLuckyBlockButton();
        builder
            .setName(Text.literal(entity))
            .setCallback(() -> ListPlayerWarningsGui.make(getBackendGui(), getPlayer(), entity).open());

        return builder.build();
    }

}

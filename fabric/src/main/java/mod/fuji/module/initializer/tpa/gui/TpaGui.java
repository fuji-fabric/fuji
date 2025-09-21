package mod.fuji.module.initializer.tpa.gui;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import mod.fuji.core.auxiliary.minecraft.GuiHelper;
import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.gui.component.gui.PagedGui;
import mod.fuji.module.initializer.tpa.service.TpaService;
import java.util.List;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TpaGui extends PagedGui<ServerPlayerEntity> {

    public TpaGui(@Nullable SimpleGui parent, @NotNull ServerPlayerEntity player, @NotNull List<ServerPlayerEntity> entities, int pageIndex) {
        super(parent, player, TextHelper.getTextByKey(player, "tpa.gui.title"), entities, pageIndex);

        GuiHelper.Placer.setSlotInLastLine(this, 4, GuiHelper.Button
            .makeHelpButton(player)
            .setLore(TextHelper.getTextListByKey(player, "tpa.gui.help.lore")));
    }

    @Override
    protected PagedGui<ServerPlayerEntity> makePage(@Nullable SimpleGui parent, @NotNull ServerPlayerEntity player, Text title, @NotNull List<ServerPlayerEntity> entities, int pageIndex) {
        return new TpaGui(parent, player, entities, pageIndex);
    }

    public static @NotNull TpaGui make(@NotNull ServerPlayerEntity player) {
        List<ServerPlayerEntity> entities = PlayerHelper.Lookup
            .getOnlinePlayers()
            .stream()
            .filter(it -> !it.equals(player))
            .toList();
        return new TpaGui(null, player, entities, 0);
    }

    @Override
    protected @NotNull GuiElementInterface toGuiElement(@NotNull ServerPlayerEntity entity) {
        GuiElementBuilder builder = GuiHelper.Button
            .makeLuckyBlockButton()
            .setName(TextHelper.getTextByKey(player, "player.name", PlayerHelper.getPlayerName(entity)))
            .setCallback(clickType -> onEntityClicked(clickType,entity));

        return builder.build();
    }

    @Override
    protected void drawPagedGui() {
        super.drawPagedGui();
        GuiHelper.PlayerSkull.fillPlayerHeadTextures(this);
    }

    @SuppressWarnings("UnnecessaryReturnStatement")
    private void onEntityClicked(@NotNull ClickType clickType, @NotNull ServerPlayerEntity entity) {
        if (clickType.isLeft) {
            TpaService.doRequest(player, entity, false);
            close();
            return;
        }

        if (clickType.isRight) {
            TpaService.doRequest(player, entity, true);
            close();
            return;
        }
    }
}

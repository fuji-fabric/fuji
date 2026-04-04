package mod.fuji.module.initializer.tpa.gui;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import mod.fuji.core.auxiliary.minecraft.GuiHelper;
import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.gui.component.gui.PagedGui;
import mod.fuji.core.gui.structure.GuiElementIR;
import mod.fuji.module.initializer.tpa.service.TpaService;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TpaGui extends PagedGui<ServerPlayer> {

    public TpaGui(@Nullable SimpleGui parent, @NotNull ServerPlayer player, @NotNull List<ServerPlayer> entities, int pageIndex) {
        super(parent, player, TextHelper.getTextByKey(player, "tpa.gui.title"), entities, pageIndex);

        GuiHelper.Placer.setSlotInLastLine(this, 4, GuiHelper.Button
            .makeHelpButton(player)
            .setLore(TextHelper.getTextListByKey(player, "tpa.gui.help.lore")));
    }

    @Override
    protected @NotNull PagedGui<ServerPlayer> makePage(@Nullable SimpleGui parent, @NotNull ServerPlayer player, Component title, @NotNull List<ServerPlayer> entities, int pageIndex) {
        return new TpaGui(parent, player, entities, pageIndex);
    }

    public static @NotNull TpaGui make(@NotNull ServerPlayer player) {
        List<ServerPlayer> entities = PlayerHelper.Lookup
            .getOnlinePlayers()
            .stream()
            .filter(it -> !it.equals(player))
            .toList();
        return new TpaGui(null, player, entities, 0);
    }

    @Override
    protected @NotNull GuiElementIR toGuiElement(@NotNull ServerPlayer entity) {
        GuiElementBuilder builder = GuiHelper.Button
            .makeLuckyBlockButton()
            .setName(TextHelper.getTextByKey(player, "player.name", PlayerHelper.getPlayerName(entity)))
            .setCallback(clickType -> onEntityClicked(clickType,entity));

        return GuiElementIR.of(builder.build());
    }

    @Override
    protected void drawPagedGui() {
        super.drawPagedGui();
        GuiHelper.PlayerSkull.fillPlayerHeadTextures(this);
    }

    @SuppressWarnings("UnnecessaryReturnStatement")
    private void onEntityClicked(@NotNull ClickType clickType, @NotNull ServerPlayer entity) {
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

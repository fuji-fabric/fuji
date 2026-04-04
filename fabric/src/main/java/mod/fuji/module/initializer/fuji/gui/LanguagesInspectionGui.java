package mod.fuji.module.initializer.fuji.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import mod.fuji.core.auxiliary.minecraft.GuiHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.gui.component.gui.PagedGui;
import java.util.ArrayList;
import java.util.List;
import mod.fuji.core.gui.structure.GuiElementIR;
import net.minecraft.world.item.Items;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LanguagesInspectionGui extends PagedGui<GuiElementIR> {

    public LanguagesInspectionGui(@Nullable SimpleGui parent, @NotNull ServerPlayer player, @NotNull List<GuiElementIR> entities, int pageIndex) {
        super(parent, player, TextHelper.getTextByKey(player, "fuji.inspect.languages.gui.title"), entities, pageIndex);
    }

    public static LanguagesInspectionGui inspectAll(@Nullable SimpleGui parent, @NotNull ServerPlayer player) {
        ArrayList<GuiElementIR> entities = new ArrayList<>();

        /* Add loaded language files. */
        TextHelper.Loader
            .LANGUAGE_CODE_2_LANGUAGE_JSON
            .forEach((languageCode, languageJson) -> {
                int numberOfUsers = TextHelper.Loader
                    .PLAYER_2_LANGUAGE_CODE
                    .values()
                    .stream()
                    .filter(it -> it.equals(languageCode))
                    .toList()
                    .size();

                boolean isSupportedLanguage = !TextHelper.Loader.isUnSupportedLanguageJsonObject(languageJson);
                boolean isDefaultLanguage = TextHelper.Loader.isDefaultLanguageCode(languageCode);

                GuiElementBuilder builder = new GuiElementBuilder()
                    .setItem(isDefaultLanguage ? Items.MAP : Items.PAPER)
                    .setName(TextHelper.getTextByKey(player, "language.name", languageCode))
                    .setLore(List.of(
                        TextHelper.getTextByKey(player, "language.is_supported", isSupportedLanguage)
                        , TextHelper.getTextByKey(player, "language.is_default", isDefaultLanguage)
                        , TextHelper.getTextByKey(player, "language.number_of_users", numberOfUsers)));

                entities.add(GuiElementIR.of(builder.build()));
            });

        entities.addAll(GuiHelper.Placer.makeLinePaddingElements(TextHelper.Loader.LANGUAGE_CODE_2_LANGUAGE_JSON.size()));

        /* Add players and their current using language code. */
        TextHelper.Loader
            .PLAYER_2_LANGUAGE_CODE
            .forEach((playerName, languageCode) -> {
                GuiElementBuilder builder = new GuiElementBuilder()
                    .setItem(Items.PLAYER_HEAD)
                    .setName(TextHelper.getTextByKey(player, "player.name", playerName))
                    .setLore(List.of(
                        TextHelper.getTextByKey(player, "language.code", languageCode)));
                entities.add(GuiElementIR.of(builder.build()));
            });

        return new LanguagesInspectionGui(parent, player, entities, 0);
    }

    @Override
    protected void drawPagedGui() {
        super.drawPagedGui();
        GuiHelper.PlayerSkull.fillPlayerHeadTextures(this);
    }

    @Override
    protected @NotNull PagedGui<GuiElementIR> makePage(@Nullable SimpleGui parent, @NotNull ServerPlayer player, Component title, @NotNull List<GuiElementIR> entities, int pageIndex) {
        return new LanguagesInspectionGui(parent, player, entities, pageIndex);
    }

    @Override
    protected @NotNull GuiElementIR toGuiElement(@NotNull GuiElementIR entity) {
        return entity;
    }

}

package io.github.sakurawald.fuji.module.initializer.fuji.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.GuiHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.gui.impl.gui.PagedGui;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LanguagesInspectionGui extends PagedGui<GuiElementInterface> {

    public LanguagesInspectionGui(@Nullable SimpleGui parent, @NotNull ServerPlayerEntity player, @NotNull List<GuiElementInterface> entities, int pageIndex) {
        super(parent, player, TextHelper.getTextByKey(player, "fuji.inspect.languages.gui.title"), entities, pageIndex);
    }

    public static LanguagesInspectionGui inspectAll(@Nullable SimpleGui parent, @NotNull ServerPlayerEntity player) {
        ArrayList<GuiElementInterface> entities = new ArrayList<>();

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

                entities.add(builder.build());
            });

        entities.addAll(GuiHelper.Filler.makeLinePaddingElements(TextHelper.Loader.LANGUAGE_CODE_2_LANGUAGE_JSON.size()));

        /* Add players and their current using language code. */
        TextHelper.Loader
            .PLAYER_2_LANGUAGE_CODE
            .forEach((playerName, languageCode) -> {
                GuiElementBuilder builder = new GuiElementBuilder()
                    .setItem(Items.PLAYER_HEAD)
                    .setName(TextHelper.getTextByKey(player, "player.name", playerName))
                    .setLore(List.of(
                        TextHelper.getTextByKey(player, "language.code", languageCode)));
                entities.add(builder.build());
            });

        return new LanguagesInspectionGui(parent, player, entities, 0);
    }

    @Override
    protected PagedGui<GuiElementInterface> make(@Nullable SimpleGui parent, ServerPlayerEntity player, Text title, @NotNull List<GuiElementInterface> entities, int pageIndex) {
        return new LanguagesInspectionGui(parent, player, entities, pageIndex);
    }

    @Override
    protected GuiElementInterface toGuiElement(GuiElementInterface entity) {
        return entity;
    }

}

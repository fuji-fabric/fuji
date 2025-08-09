package io.github.sakurawald.fuji.module.initializer.title.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.GuiHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.gui.impl.gui.PagedGui;
import io.github.sakurawald.fuji.module.initializer.title.TitleInitializer;
import io.github.sakurawald.fuji.module.initializer.title.service.TitleService;
import io.github.sakurawald.fuji.module.initializer.title.structure.TitleDescriptor;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ListTitlesGui extends PagedGui<TitleDescriptor> {

    public ListTitlesGui(@Nullable SimpleGui parent, @NotNull ServerPlayerEntity player, @NotNull List<TitleDescriptor> entities, int pageIndex) {
        super(parent, player, TextHelper.getTextByKey(player, "title.list.gui.title"), entities, pageIndex);

        drawInfoButton();

        getFooter().setSlot(4, GuiHelper.Button
            .makeHelpButton(player)
            .setLore(TextHelper.getTextListByKey(player, "title.list.help.lore")));

        if (isViewingAllTitles(entities)) {
            getFooter().setSlot(5, GuiHelper.Button
                .makeLetterAButton()
                .setName(TextHelper.getTextByKey(player, "entity.list.mine"))
                .setCallback(() -> makeInstance(player, TitleService.getObtainedTitles(player)).open())
            );
        } else {
            getFooter().setSlot(5, GuiHelper.Button
                .makeHeartButton()
                .setName(TextHelper.getTextByKey(player, "entity.list.all"))
                .setCallback(() -> makeInstance(player, TitleService.getAllTitles()).open())
            );
        }
    }

    private void drawInfoButton() {
        ServerPlayerEntity player = this.getPlayer();

        Optional<String> activeTitle = TitleService
            .getActiveTitle(player)
            .map(TitleDescriptor::getDisplayName);

        getFooter().setSlot(3, GuiHelper.Button
            .makeInfoButton(player)
            .setLore(List.of(
                TextHelper.getTextByKey(player, "title.title.active", activeTitle.orElse(TitleService.getNoTitleActiveText()))
            )));

        markDirty();
    }

    private boolean isViewingAllTitles(@NotNull List<TitleDescriptor> entities) {
        return entities == TitleInitializer.config.model().getTitleDescriptors();
    }

    public static ListTitlesGui makeInstance(@NotNull ServerPlayerEntity player, List<TitleDescriptor> entities) {
        return new ListTitlesGui(null, player, entities, 0);
    }

    @Override
    protected PagedGui<TitleDescriptor> make(@Nullable SimpleGui parent, @NotNull ServerPlayerEntity player, Text title, @NotNull List<TitleDescriptor> entities, int pageIndex) {
        return new ListTitlesGui(parent, player, entities, pageIndex);
    }

    @Override
    protected @NotNull GuiElementInterface toGuiElement(@NotNull TitleDescriptor entity) {
        ServerPlayerEntity player = getPlayer();

        GuiElementBuilder builder = new GuiElementBuilder();
        builder.setItem(entity.toItem());
        builder.setName(TextHelper.getTextByValue(player, entity.getDisplayName()));

        List<Text> lore = new ArrayList<>();
        lore.add(TextHelper.getTextByKey(player, "entity.obtained", TitleService.isTitleObtained(player, entity.getId()), entity.getId()));
        lore.addAll(TextHelper.getTextListByValue(player, entity.getLore()));
        builder.setLore(lore);

        builder.setCallback((clickType) -> {
            if (clickType.shift && clickType.isLeft) {
                Optional<TitleDescriptor> activeTitle = TitleService.getActiveTitle(player);
                activeTitle.ifPresent(descriptor -> {
                    TitleService.setActiveTitle(player, null);
                    TextHelper.sendTextByKey(player, "title.title.active.unset", descriptor.getDisplayName());
                    this.drawInfoButton();
                });
                return;
            }

            if (clickType.isLeft) {
                if (!TitleService.isTitleObtained(player, entity.getId())) {
                    TextHelper.sendTextByKey(player, "title.not_obtained", entity.getDisplayName());
                    return;
                }

                TitleService.setActiveTitle(player, entity.getId());
                this.drawInfoButton();

                TextHelper.sendTextByKey(player, "title.title.active.set", entity.getDisplayName());
                return;
            }

        });

        return builder.build();
    }
}

package io.github.sakurawald.fuji.module.initializer.home.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.GuiHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.gui.component.gui.ConfirmSignGui;
import io.github.sakurawald.fuji.core.gui.component.gui.PagedGui;
import io.github.sakurawald.fuji.core.structure.GlobalPos;
import io.github.sakurawald.fuji.module.initializer.home.service.HomeService;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ListHomesGui extends PagedGui<GlobalPos> {

    private final String targetPlayerName;

    private ListHomesGui(@Nullable SimpleGui parent, @NotNull ServerPlayerEntity player, @NotNull String targetPlayerName, @NotNull List<GlobalPos> entities, int pageIndex) {
        super(parent, player, TextHelper.getTextByKey(player, "home.list.gui.title", targetPlayerName), entities, pageIndex);
        this.targetPlayerName = targetPlayerName;

        GuiHelper.Placer.setSlotInLastLine(this, 4, GuiHelper.Button
            .makeHelpButton(player)
            .setLore(TextHelper.getTextListByKey(player, "home.list.gui.help.lore"))
        );
    }

    public static ListHomesGui make(@NotNull ServerPlayerEntity player, @NotNull String targetPlayerName) {
        List<GlobalPos> entities = HomeService
            .withHomeMap(targetPlayerName)
            .values()
            .stream().toList();
        return new ListHomesGui(null, player, targetPlayerName, entities, 0);
    }

    @Override
    protected PagedGui<GlobalPos> makePage(@Nullable SimpleGui parent, @NotNull ServerPlayerEntity player, Text title, @NotNull List<GlobalPos> entities, int pageIndex) {
        return new ListHomesGui(parent, player, this.targetPlayerName, entities, pageIndex);
    }

    @SuppressWarnings({"CollectionAddAllCanBeReplacedWithConstructor", "UnnecessaryReturnStatement"})
    @Override
    protected @NotNull GuiElementInterface toGuiElement(@NotNull GlobalPos entity) {
        String homeName = HomeService
            .withHomeMap(targetPlayerName)
            .inverse()
            .get(entity);

        List<Text> lore = new ArrayList<>();
        lore.addAll(entity.asLore(player));
        lore.add(TextHelper.getTextByKey(player, "prompt.click.teleport"));

        return new GuiElementBuilder()
            .setItem(Items.PINK_BED)
            .setName(Text.literal(homeName))
            .setLore(lore)
            .setCallback((clickType) -> {
                if (clickType.isLeft) {
                    entity.teleport(player);
                    return;
                }

                if (clickType.isRight) {
                    new ConfirmSignGui(player) {
                        @Override
                        public void onConfirm() {
                            HomeService.removeHome(targetPlayerName, homeName);
                            TextHelper.sendTextByKey(player, "home.unset.success", homeName);
                        }
                    }.open();
                    return;
                }

            })
            .build();
    }
}

package mod.fuji.module.initializer.home.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import mod.fuji.core.auxiliary.minecraft.GuiHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.gui.component.gui.ConfirmSignGui;
import mod.fuji.core.gui.component.gui.PagedGui;
import mod.fuji.core.gui.structure.GuiElementIR;
import mod.fuji.core.structure.GlobalPos;
import mod.fuji.module.initializer.home.service.HomeService;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.Items;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ListHomesGui extends PagedGui<GlobalPos> {

    private final String targetPlayerName;

    private ListHomesGui(@Nullable SimpleGui parent, @NotNull ServerPlayer player, @NotNull String targetPlayerName, @NotNull List<GlobalPos> entities, int pageIndex) {
        super(parent, player, TextHelper.getTextByKey(player, "home.list.gui.title", targetPlayerName), entities, pageIndex);
        this.targetPlayerName = targetPlayerName;

        GuiHelper.Placer.setSlotInLastLine(this, 4, GuiHelper.Button
            .makeHelpButton(player)
            .setLore(TextHelper.getTextListByKey(player, "home.list.gui.help.lore"))
        );
    }

    public static ListHomesGui make(@NotNull ServerPlayer player, @NotNull String targetPlayerName) {
        List<GlobalPos> entities = HomeService
            .withHomeMap(targetPlayerName)
            .values()
            .stream().toList();
        return new ListHomesGui(null, player, targetPlayerName, entities, 0);
    }

    @Override
    protected @NotNull PagedGui<GlobalPos> makePage(@Nullable SimpleGui parent, @NotNull ServerPlayer player, Component title, @NotNull List<GlobalPos> entities, int pageIndex) {
        return new ListHomesGui(parent, player, this.targetPlayerName, entities, pageIndex);
    }

    @SuppressWarnings({"CollectionAddAllCanBeReplacedWithConstructor", "UnnecessaryReturnStatement"})
    @Override
    protected @NotNull GuiElementIR toGuiElement(@NotNull GlobalPos entity) {
        String homeName = HomeService
            .withHomeMap(targetPlayerName)
            .inverse()
            .get(entity);

        List<Component> lore = new ArrayList<>();
        lore.addAll(entity.asLore(player));
        lore.add(TextHelper.getTextByKey(player, "prompt.click.teleport"));

        return GuiElementIR.of(new GuiElementBuilder()
            .setItem(Items.PINK_BED)
            .setName(Component.literal(homeName))
            .setLore(lore)
            .setCallback((clickType) -> {
                if (clickType.isLeft) {
                    entity.teleport(player);
                    close();
                    return;
                }

                if (clickType.isRight) {
                    new ConfirmSignGui(player) {
                        @Override
                        public void onConfirm() {
                            HomeService.removeHome(targetPlayerName, homeName);
                            TextHelper.sendTextByKey(player, "home.unset.success", homeName);
                        }

                        @Override
                        protected void onConfirmedOrCancelled() {
                            getBackendGui().open();
                        }
                    }.open();
                    return;
                }

            })
            .build());
    }
}

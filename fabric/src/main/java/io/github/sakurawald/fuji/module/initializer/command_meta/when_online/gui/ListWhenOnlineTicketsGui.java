package io.github.sakurawald.fuji.module.initializer.command_meta.when_online.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.sakurawald.fuji.core.auxiliary.ChronosUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.GuiHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.gui.component.gui.ConfirmSignGui;
import io.github.sakurawald.fuji.core.gui.component.gui.PagedGui;
import io.github.sakurawald.fuji.module.initializer.command_meta.when_online.WhenOnlineInitializer;
import io.github.sakurawald.fuji.module.initializer.command_meta.when_online.structure.WhenOnlineTicket;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.item.Item;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ListWhenOnlineTicketsGui extends PagedGui<WhenOnlineTicket> {

    public ListWhenOnlineTicketsGui(@Nullable SimpleGui parent, @NotNull ServerPlayerEntity player, @NotNull List<WhenOnlineTicket> entities, int pageIndex) {
        super(parent, player, TextHelper.getTextByKey(player, "command_meta.when_online.gui.title"), entities, pageIndex)
        ;
    }

    @Override
    protected PagedGui<WhenOnlineTicket> makePage(@Nullable SimpleGui parent, @NotNull ServerPlayerEntity player, Text title, @NotNull List<WhenOnlineTicket> entities, int pageIndex) {
        return new ListWhenOnlineTicketsGui(parent, player, entities, pageIndex);
    }

    public static ListWhenOnlineTicketsGui make(ServerPlayerEntity player) {
        List<WhenOnlineTicket> tickets = WhenOnlineInitializer.data.model().tickets
            .stream()
            // Put the un-executed tickets first.
            // Put the tickets created earlier first.
            .sorted(Comparator
                .comparing(WhenOnlineTicket::isExecuted)
                .thenComparing(WhenOnlineTicket::getCreatedTimestamp))
            .toList();
        return new ListWhenOnlineTicketsGui(null, player, tickets, 0);
    }

    @Override
    protected @NotNull GuiElementInterface toGuiElement(@NotNull WhenOnlineTicket entity) {
        GuiElementBuilder builder = new GuiElementBuilder();

        List<Text> lore = new ArrayList<>();
        lore.add(TextHelper.getTextByKey(getPlayer(), "entity.created_timestamp", ChronosUtil.Formatter.formatDate(entity.createdTimestamp)));
        lore.add(TextHelper.getTextByKey(getPlayer(), "entity.creator_name", entity.creatorName));
        lore.add(TextHelper.getTextByKey(getPlayer(), "player.target_player.name", entity.getTargetPlayer()));
        lore.add(TextHelper.getTextByKey(getPlayer(), "command", TextHelper.Parsers.escapeTags(entity.command)));
        lore.add(TextHelper.getTextByKey(getPlayer(), "entity.executed_timestamp", ChronosUtil.Formatter.formatDate(entity.executedTimestamp)));

        lore.add(TextHelper.getTextByKey(getPlayer(), "prompt.click.delete.right_click"));

        Item item = GuiHelper.Material.fromBooleanValue(entity.isExecuted());
        builder
            .setItem(item)
            .setName(TextHelper.getTextByKey(getPlayer(),"command_meta.when_online.gui.name"))
            .setLore(lore)
            .setCallback(onClickEntity(getBackendGui(), getPlayer(), entity));

        return builder.build();
    }

    private static GuiElementInterface.@NotNull ItemClickCallback onClickEntity(SimpleGui gui, ServerPlayerEntity player, WhenOnlineTicket entity) {
        return (a, b, c) -> {
            // Right click -> delete.
            if (b.isRight) {
                new ConfirmSignGui(player) {
                    @Override
                    public void onConfirm() {
                        WhenOnlineInitializer.data.model()
                            .tickets.remove(entity);
                        WhenOnlineInitializer.data.writeStorage();
                        ListWhenOnlineTicketsGui.make(player)
                            .open();
                    }

                    @Override
                    protected void onCancelled() {
                        gui.open();
                    }
                }.open();
            }
        };
    }

}

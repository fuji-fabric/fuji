package mod.fuji.module.initializer.command_meta.when_online.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import mod.fuji.core.auxiliary.ChronosUtil;
import mod.fuji.core.auxiliary.minecraft.GuiHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.gui.component.gui.ConfirmSignGui;
import mod.fuji.core.gui.component.gui.PagedGui;
import mod.fuji.module.initializer.command_meta.when_online.WhenOnlineInitializer;
import mod.fuji.module.initializer.command_meta.when_online.structure.WhenOnlineTicket;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.world.item.Item;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ListWhenOnlineTicketsGui extends PagedGui<WhenOnlineTicket> {

    public ListWhenOnlineTicketsGui(@Nullable SimpleGui parent, @NotNull ServerPlayer player, @NotNull List<WhenOnlineTicket> entities, int pageIndex) {
        super(parent, player, TextHelper.getTextByKey(player, "command_meta.when_online.gui.title"), entities, pageIndex)
        ;
    }

    @Override
    protected @NotNull PagedGui<WhenOnlineTicket> makePage(@Nullable SimpleGui parent, @NotNull ServerPlayer player, Component title, @NotNull List<WhenOnlineTicket> entities, int pageIndex) {
        return new ListWhenOnlineTicketsGui(parent, player, entities, pageIndex);
    }

    public static ListWhenOnlineTicketsGui make(ServerPlayer player) {
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

        List<Component> lore = new ArrayList<>();
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

    private static GuiElementInterface.@NotNull ItemClickCallback onClickEntity(SimpleGui gui, ServerPlayer player, WhenOnlineTicket entity) {
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

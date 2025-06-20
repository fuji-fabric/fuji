package io.github.sakurawald.module.initializer.chat.display.helper;

import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.manager.Managers;
import io.github.sakurawald.module.initializer.chat.display.ChatDisplayInitializer;
import io.github.sakurawald.core.service.display.gui.BaseDisplayGuiFactory;
import io.github.sakurawald.module.initializer.chat.display.gui.EnderChestDisplayGuiFactory;
import io.github.sakurawald.core.service.display.gui.InventoryDisplayGuiFactory;
import io.github.sakurawald.module.initializer.chat.display.gui.ItemDisplayGuiFactory;
import io.github.sakurawald.core.service.display.gui.ShulkerBoxDisplayGuiFactory;
import io.github.sakurawald.module.initializer.chat.display.structure.SoftReferenceMap;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class DisplayHelper {

    private static final SoftReferenceMap<String, BaseDisplayGuiFactory> uuid2gui = new SoftReferenceMap<>();

    private static String bindUUID(BaseDisplayGuiFactory displayGuiFactory) {
        String uuid = UUID.randomUUID().toString();
        uuid2gui.put(uuid, displayGuiFactory);
        return uuid;
    }

    private static void viewDisplayGui(@NotNull ServerPlayerEntity viewerPlayer, String displayUUID) {
        BaseDisplayGuiFactory baseDisplayGui = uuid2gui.get(displayUUID);
        if (baseDisplayGui == null) {
            TextHelper.sendMessageByKey(viewerPlayer, "display.invalid");
            return;
        }
        baseDisplayGui.build(viewerPlayer).open();
    }

    public static MutableText createEnderDisplayText(ServerPlayerEntity player) {
        String displayUUID = bindUUID(new EnderChestDisplayGuiFactory(player));
        return TextHelper.getTextByKey(player, "display.ender_chest.text")
            .copy()
            .fillStyle(
                Style.EMPTY
                    .withHoverEvent(TextHelper.HoverEvent.makeShowTextAction(TextHelper.getTextByKey(player, "display.click.prompt")))
                    .withClickEvent(makeDisplayClickEvent(displayUUID))
            );
    }

    public static MutableText createInvDisplayText(ServerPlayerEntity player) {
        String displayUUID = bindUUID(new InventoryDisplayGuiFactory(player));
        return TextHelper.getTextByKey(player, "display.inventory.text")
            .copy()
            .fillStyle(Style.EMPTY
                .withHoverEvent(TextHelper.HoverEvent.makeShowTextAction(TextHelper.getTextByKey(player, "display.click.prompt")))
                .withClickEvent(makeDisplayClickEvent(displayUUID))
            );
    }

    public static @NotNull MutableText createItemDisplayText(ServerPlayerEntity player) {
        /* Make the display gui. */
        BaseDisplayGuiFactory displayGui;
        ItemStack itemStack = player.getMainHandStack().copy();
        if (BaseDisplayGuiFactory.isShulkerBox(itemStack)) {
            displayGui = new ShulkerBoxDisplayGuiFactory(player, itemStack, null);
        } else {
            displayGui = new ItemDisplayGuiFactory(player, itemStack);
        }

        /* Bind UUID for GUI. */
        String displayUUID = bindUUID(displayGui);

        /* Make display text. */
        MutableText translatable = Text.translatable(player.getMainHandStack().getItem().getTranslationKey());
        translatable.fillStyle(Style.EMPTY
            .withHoverEvent(TextHelper.HoverEvent.makeShowTextAction(TextHelper.getTextByKey(player, "display.click.prompt")))
            .withClickEvent(makeDisplayClickEvent(displayUUID))
        );

        MutableText text = TextHelper.getTextByKey(player, "display.item.text").copy();
        text = TextHelper.replaceTextWithMarker(text, "item", () -> translatable);
        return text;
    }

    @NotNull
    private static ClickEvent makeDisplayClickEvent(String displayUUID) {
        return Managers
            .getCallbackManager()
            .makeCallbackEvent((player) -> viewDisplayGui(player, displayUUID), ChatDisplayInitializer.config.model().expiration_duration_s, TimeUnit.SECONDS);
    }
}

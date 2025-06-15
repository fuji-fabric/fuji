package io.github.sakurawald.module.initializer.chat.display.helper;

import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.manager.Managers;
import io.github.sakurawald.module.initializer.chat.display.ChatDisplayInitializer;
import io.github.sakurawald.module.initializer.chat.display.gui.BaseDisplayGui;
import io.github.sakurawald.module.initializer.chat.display.gui.EnderChestDisplayGui;
import io.github.sakurawald.module.initializer.chat.display.gui.InventoryDisplayGui;
import io.github.sakurawald.module.initializer.chat.display.gui.ItemDisplayGui;
import io.github.sakurawald.module.initializer.chat.display.gui.ShulkerBoxDisplayGui;
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

    private static final SoftReferenceMap<String, BaseDisplayGui> uuid2gui = new SoftReferenceMap<>();

    private static String bindUUID(BaseDisplayGui displayGui) {
        String uuid = UUID.randomUUID().toString();
        uuid2gui.put(uuid, displayGui);
        return uuid;
    }

    private static void viewDisplayGui(@NotNull ServerPlayerEntity viewerPlayer, String displayUUID) {
        BaseDisplayGui baseDisplayGui = uuid2gui.get(displayUUID);
        if (baseDisplayGui == null) {
            TextHelper.sendMessageByKey(viewerPlayer, "display.invalid");
            return;
        }
        baseDisplayGui.build(viewerPlayer).open();
    }

    public static MutableText createEnderDisplayText(ServerPlayerEntity player) {
        String displayUUID = bindUUID(new EnderChestDisplayGui(player));
        return TextHelper.getTextByKey(player, "display.ender_chest.text")
            .copy()
            .fillStyle(
                Style.EMPTY
                    .withHoverEvent(TextHelper.HoverEvent.makeShowTextAction(TextHelper.getTextByKey(player, "display.click.prompt")))
                    .withClickEvent(makeDisplayClickEvent(displayUUID))
            );
    }

    public static MutableText createInvDisplayText(ServerPlayerEntity player) {
        String displayUUID = bindUUID(new InventoryDisplayGui(player));
        return TextHelper.getTextByKey(player, "display.inventory.text")
            .copy()
            .fillStyle(Style.EMPTY
                .withHoverEvent(TextHelper.HoverEvent.makeShowTextAction(TextHelper.getTextByKey(player, "display.click.prompt")))
                .withClickEvent(makeDisplayClickEvent(displayUUID))
            );
    }

    public static @NotNull MutableText createItemDisplayText(ServerPlayerEntity player) {
        /* Make the display gui. */
        BaseDisplayGui displayGui;
        ItemStack itemStack = player.getMainHandStack().copy();
        if (BaseDisplayGui.isShulkerBox(itemStack)) {
            displayGui = new ShulkerBoxDisplayGui(player, itemStack, null);
        } else {
            displayGui = new ItemDisplayGui(player, itemStack);
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

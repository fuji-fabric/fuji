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
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class DisplayHelper {

    private static final SoftReferenceMap<String, BaseDisplayGui> uuid2gui = new SoftReferenceMap<>();

    private static String makeInventoryDisplayUuid(@NotNull ServerPlayerEntity player) {
        Text title = TextHelper.getTextByKey(player, "display.gui.title", player.getGameProfile().getName());
        String uuid = UUID.randomUUID().toString();
        uuid2gui.put(uuid, new InventoryDisplayGui(title, player));
        return uuid;
    }

    private static String makeEnderChestDisplayUuid(@NotNull ServerPlayerEntity player) {
        Text title = TextHelper.getTextByKey(player, "display.gui.title", player.getGameProfile().getName());
        String uuid = UUID.randomUUID().toString();
        uuid2gui.put(uuid, new EnderChestDisplayGui(title, player));
        return uuid;
    }

    private static String makeItemDisplayUuid(@NotNull ServerPlayerEntity player) {
        /* new object */
        BaseDisplayGui baseDisplayGui;
        Text title = TextHelper.getTextByKey(player, "display.gui.title", player.getGameProfile().getName());
        ItemStack itemStack = player.getMainHandStack().copy();
        if (BaseDisplayGui.isShulkerBox(itemStack)) {
            // shulker-box item
            baseDisplayGui = new ShulkerBoxDisplayGui(title, itemStack, null);
        } else {
            // non-shulker-box item
            baseDisplayGui = new ItemDisplayGui(title, itemStack);
        }

        /* put object */
        String uuid = UUID.randomUUID().toString();
        uuid2gui.put(uuid, baseDisplayGui);
        return uuid;
    }

    public static void viewDisplay(@NotNull ServerPlayerEntity player, String displayUUID) {
        BaseDisplayGui baseDisplayGui = uuid2gui.get(displayUUID);
        if (baseDisplayGui == null) {
            TextHelper.sendMessageByKey(player, "display.invalid");
            return;
        }
        baseDisplayGui.build(player).open();
    }

    public static MutableText createEnderDisplayText(ServerPlayerEntity player) {
        String displayUUID = makeEnderChestDisplayUuid(player);
        return TextHelper.getTextByKey(player, "display.ender_chest.text")
            .copy()
            .fillStyle(
                Style.EMPTY
                    .withHoverEvent(new HoverEvent.ShowText(TextHelper.getTextByKey(player, "display.click.prompt")))
                    .withClickEvent(makeDisplayClickEvent(displayUUID))
            );
    }

    public static MutableText createInvDisplayText(ServerPlayerEntity player) {
        String displayUUID = makeInventoryDisplayUuid(player);
        return TextHelper.getTextByKey(player, "display.inventory.text")
            .copy()
            .fillStyle(Style.EMPTY
                .withHoverEvent(new HoverEvent.ShowText(TextHelper.getTextByKey(player, "display.click.prompt")))
                .withClickEvent(makeDisplayClickEvent(displayUUID))
            );
    }

    public static @NotNull MutableText createItemDisplayText(ServerPlayerEntity player) {
        String displayUUID = makeItemDisplayUuid(player);
        MutableText text = TextHelper.getTextByKey(player, "display.item.text").copy();

        MutableText translatable = Text.translatable(player.getMainHandStack().getItem().getTranslationKey());
        translatable.fillStyle(Style.EMPTY
            .withHoverEvent(new HoverEvent.ShowText(TextHelper.getTextByKey(player, "display.click.prompt")))
            .withClickEvent(makeDisplayClickEvent(displayUUID))
        );

        text = TextHelper.replaceTextWithMarker(text, "item", () -> translatable);
        return text;
    }

    @NotNull
    private static ClickEvent makeDisplayClickEvent(String displayUUID) {
        return Managers.getCallbackManager().makeCallbackEvent((player) -> viewDisplay(player, displayUUID), ChatDisplayInitializer.config.model().expiration_duration_s, TimeUnit.SECONDS);
    }
}

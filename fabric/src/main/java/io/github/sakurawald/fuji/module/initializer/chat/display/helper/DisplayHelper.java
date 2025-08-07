package io.github.sakurawald.fuji.module.initializer.chat.display.helper;

import io.github.sakurawald.fuji.core.auxiliary.RandomUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.manager.Managers;
import io.github.sakurawald.fuji.module.initializer.chat.display.ChatDisplayInitializer;
import io.github.sakurawald.fuji.core.service.display.gui.BaseDisplayGuiFactory;
import io.github.sakurawald.fuji.module.initializer.chat.display.gui.EnderChestDisplayGuiFactory;
import io.github.sakurawald.fuji.core.service.display.gui.InventoryDisplayGuiFactory;
import io.github.sakurawald.fuji.module.initializer.chat.display.gui.ItemDisplayGuiFactory;
import io.github.sakurawald.fuji.core.service.display.gui.ShulkerBoxDisplayGuiFactory;
import io.github.sakurawald.fuji.module.initializer.chat.display.structure.SoftReferenceMap;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class DisplayHelper {

    private static final SoftReferenceMap<String, BaseDisplayGuiFactory> uuid2gui = new SoftReferenceMap<>();

    private static String bindUUID(BaseDisplayGuiFactory displayGuiFactory) {
        String uuid = RandomUtil.randomUUID();
        uuid2gui.put(uuid, displayGuiFactory);
        return uuid;
    }

    private static void viewDisplayGui(@NotNull ServerPlayerEntity viewingPlayer, String displayUUID) {
        BaseDisplayGuiFactory baseDisplayGui = uuid2gui.get(displayUUID);
        if (baseDisplayGui == null) {
            TextHelper.sendTextByKey(viewingPlayer, "display.invalid");
            return;
        }
        baseDisplayGui.build(viewingPlayer).open();
    }

    public static MutableText createEnderDisplayText(ServerPlayerEntity sharingPlayer) {
        String displayUUID = bindUUID(new EnderChestDisplayGuiFactory(sharingPlayer));
        return TextHelper.getTextByKey(sharingPlayer, "display.ender_chest.text")
            .copy()
            .fillStyle(
                Style.EMPTY
                    .withHoverEvent(TextHelper.Events.HoverEvent.makeShowTextAction(TextHelper.getTextByKey(sharingPlayer, "display.click.prompt")))
                    .withClickEvent(makeDisplayClickEvent(displayUUID))
            );
    }

    public static MutableText createInvDisplayText(ServerPlayerEntity sharingPlayer) {
        String displayUUID = bindUUID(new InventoryDisplayGuiFactory(sharingPlayer));
        return TextHelper.getTextByKey(sharingPlayer, "display.inventory.text")
            .copy()
            .fillStyle(Style.EMPTY
                .withHoverEvent(TextHelper.Events.HoverEvent.makeShowTextAction(TextHelper.getTextByKey(sharingPlayer, "display.click.prompt")))
                .withClickEvent(makeDisplayClickEvent(displayUUID))
            );
    }

    public static @NotNull MutableText createItemDisplayText(ServerPlayerEntity sharingPlayer) {
        /* Make the display gui. */
        BaseDisplayGuiFactory displayGui;
        ItemStack itemStack = sharingPlayer.getMainHandStack().copy();
        if (BaseDisplayGuiFactory.isShulkerBox(itemStack)) {
            displayGui = new ShulkerBoxDisplayGuiFactory(sharingPlayer, itemStack, null);
        } else {
            displayGui = new ItemDisplayGuiFactory(sharingPlayer, itemStack);
        }

        /* Bind UUID for GUI. */
        String displayUUID = bindUUID(displayGui);

        /* Make display text. */
        MutableText translatable = Text.translatable(sharingPlayer.getMainHandStack().getItem().getTranslationKey());
        translatable.fillStyle(Style.EMPTY
            .withHoverEvent(TextHelper.Events.HoverEvent.makeShowTextAction(TextHelper.getTextByKey(sharingPlayer, "display.click.prompt")))
            .withClickEvent(makeDisplayClickEvent(displayUUID))
        );

        MutableText text = TextHelper.getTextByKey(sharingPlayer, "display.item.text").copy();
        text = TextHelper.Replacer.replaceTextWithNamedArgument(text, "item", (matcher) -> translatable);
        return text;
    }

    @NotNull
    private static ClickEvent makeDisplayClickEvent(String displayUUID) {
        return Managers
            .getCallbackManager()
            .makeCallbackEvent((player) -> viewDisplayGui(player, displayUUID), ChatDisplayInitializer.config.model().expiration_duration_s, TimeUnit.SECONDS);
    }
}

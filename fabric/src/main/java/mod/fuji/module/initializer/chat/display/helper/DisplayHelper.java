package mod.fuji.module.initializer.chat.display.helper;

import mod.fuji.core.auxiliary.RandomUtil;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.manager.Managers;
import mod.fuji.module.initializer.chat.display.ChatDisplayInitializer;
import mod.fuji.core.service.display.gui.BaseDisplayGuiFactory;
import mod.fuji.module.initializer.chat.display.gui.EnderChestDisplayGuiFactory;
import mod.fuji.core.service.display.gui.InventoryDisplayGuiFactory;
import mod.fuji.module.initializer.chat.display.gui.ItemDisplayGuiFactory;
import mod.fuji.core.service.display.gui.ShulkerBoxDisplayGuiFactory;
import mod.fuji.module.initializer.chat.display.structure.SoftReferenceMap;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class DisplayHelper {

    private static final SoftReferenceMap<String, BaseDisplayGuiFactory> uuid2factory = new SoftReferenceMap<>();

    private static String bindUUID(@NotNull BaseDisplayGuiFactory displayGuiFactory) {
        String uuid = RandomUtil.randomUUID();
        uuid2factory.put(uuid, displayGuiFactory);
        return uuid;
    }

    private static void viewDisplayGui(@NotNull ServerPlayerEntity viewingPlayer, @NotNull String displayUUID) {
        BaseDisplayGuiFactory baseDisplayGui = uuid2factory.get(displayUUID);
        if (baseDisplayGui == null) {
            TextHelper.sendTextByKey(viewingPlayer, "display.invalid");
            return;
        }
        baseDisplayGui
            .build(viewingPlayer)
            .open();
    }

    public static MutableText createEnderDisplayText(@NotNull ServerPlayerEntity sharingPlayer) {
        String displayUUID = bindUUID(new EnderChestDisplayGuiFactory(sharingPlayer));
        return TextHelper.getTextByKey(sharingPlayer, "display.ender_chest.text").copy()
            .fillStyle(
                Style.EMPTY
                    .withHoverEvent(TextHelper.Events.HoverEvent.makeShowTextAction(TextHelper.getTextByKey(sharingPlayer, "display.click.prompt")))
                    .withClickEvent(makeDisplayClickEvent(displayUUID))
            );
    }

    public static MutableText createInvDisplayText(@NotNull ServerPlayerEntity sharingPlayer) {
        String displayUUID = bindUUID(new InventoryDisplayGuiFactory(sharingPlayer));
        return TextHelper.getTextByKey(sharingPlayer, "display.inventory.text").copy()
            .fillStyle(Style.EMPTY
                .withHoverEvent(TextHelper.Events.HoverEvent.makeShowTextAction(TextHelper.getTextByKey(sharingPlayer, "display.click.prompt")))
                .withClickEvent(makeDisplayClickEvent(displayUUID))
            );
    }

    public static @NotNull MutableText createItemDisplayText(@NotNull ServerPlayerEntity sharingPlayer) {
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
        MutableText translatableItemNameText = Text.translatable(sharingPlayer.getMainHandStack().getItem().getTranslationKey());
        translatableItemNameText.fillStyle(Style.EMPTY
            .withHoverEvent(TextHelper.Events.HoverEvent.makeShowTextAction(TextHelper.getTextByKey(sharingPlayer, "display.click.prompt")))
            .withClickEvent(makeDisplayClickEvent(displayUUID))
        );

        MutableText displayText = TextHelper.getTextByKey(sharingPlayer, "display.item.text").copy();
        displayText = TextHelper.Replacer.replaceTextWithNamedArgument(displayText, "item", (matcher) -> translatableItemNameText);
        return displayText;
    }

    @NotNull
    private static ClickEvent makeDisplayClickEvent(@NotNull String displayUUID) {
        return Managers
            .getCallbackManager()
            .makeCallbackEvent((player) -> viewDisplayGui(player, displayUUID), ChatDisplayInitializer.config.model().getExpirationDurationSeconds(), TimeUnit.SECONDS);
    }
}

package mod.fuji.module.initializer.chat.display.service;

import java.util.Optional;
import mod.fuji.core.auxiliary.RandomUtil;
import mod.fuji.core.auxiliary.minecraft.ItemStackHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.service.command_callback.CommandCallbackManager;
import mod.fuji.module.initializer.chat.display.ChatDisplayInitializer;
import mod.fuji.core.service.display.gui.BaseDisplayGuiFactory;
import mod.fuji.module.initializer.chat.display.gui.EnderChestDisplayGuiFactory;
import mod.fuji.core.service.display.gui.InventoryDisplayGuiFactory;
import mod.fuji.module.initializer.chat.display.gui.ItemDisplayGuiFactory;
import mod.fuji.core.service.display.gui.ShulkerBoxDisplayGuiFactory;
import mod.fuji.module.initializer.chat.display.structure.SoftReferenceMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class ChatDisplayService {

    private static final SoftReferenceMap<String, BaseDisplayGuiFactory> uuid2factory = new SoftReferenceMap<>();

    private static @NotNull String bindUUID(@NotNull BaseDisplayGuiFactory displayGuiFactory) {
        String uuid = RandomUtil.randomUUID();
        uuid2factory.put(uuid, displayGuiFactory);
        return uuid;
    }

    private static void openDisplayGui(@NotNull ServerPlayer viewingPlayer, @NotNull String displayUUID) {
        Optional
            .ofNullable(uuid2factory.get(displayUUID))
            .ifPresentOrElse(baseDisplayGuiFactory -> {
                baseDisplayGuiFactory
                    .build(viewingPlayer)
                    .open();
            }, () -> TextHelper.sendTextByKey(viewingPlayer, "display.invalid"));
    }

    public static @NotNull MutableComponent createEnderDisplayText(@NotNull ServerPlayer sharingPlayer) {
        String displayUUID = bindUUID(new EnderChestDisplayGuiFactory(sharingPlayer));
        return TextHelper
            .getTextByKey(sharingPlayer, "display.ender_chest.text")
            .copy()
            .withStyle(
                Style.EMPTY
                    .withHoverEvent(TextHelper.Events.HoverEvent.makeShowTextAction(TextHelper.getTextByKey(sharingPlayer, "display.click.prompt")))
                    .withClickEvent(makeDisplayClickEvent(displayUUID))
            );
    }

    public static @NotNull MutableComponent createInvDisplayText(@NotNull ServerPlayer sharingPlayer) {
        String displayUUID = bindUUID(new InventoryDisplayGuiFactory(sharingPlayer));
        return TextHelper.getTextByKey(sharingPlayer, "display.inventory.text")
            .copy()
            .withStyle(Style.EMPTY
                .withHoverEvent(TextHelper.Events.HoverEvent.makeShowTextAction(TextHelper.getTextByKey(sharingPlayer, "display.click.prompt")))
                .withClickEvent(makeDisplayClickEvent(displayUUID))
            );
    }

    public static @NotNull MutableComponent createItemDisplayText(@NotNull ServerPlayer sharingPlayer) {
        /* Make the display gui. */
        BaseDisplayGuiFactory displayGui;
        ItemStack itemStack = sharingPlayer
            .getMainHandItem()
            .copy();

        if (ItemStackHelper.Kind.isShulkerBox(itemStack)) {
            displayGui = new ShulkerBoxDisplayGuiFactory(sharingPlayer, itemStack, null);
        } else {
            displayGui = new ItemDisplayGuiFactory(sharingPlayer, itemStack);
        }

        /* Bind UUID for GUI. */
        String displayUUID = bindUUID(displayGui);

        /* Make display text. */
        String itemStackTranslatableString = itemStack.getItem().getDescriptionId();
        MutableComponent translatableItemNameText = Component
            .translatable(itemStackTranslatableString);
        translatableItemNameText.withStyle(Style.EMPTY
            .withHoverEvent(TextHelper.Events.HoverEvent.makeShowTextAction(TextHelper.getTextByKey(sharingPlayer, "display.click.prompt")))
            .withClickEvent(makeDisplayClickEvent(displayUUID))
        );

        MutableComponent displayText = TextHelper
            .getTextByKey(sharingPlayer, "display.item.text")
            .copy();
        displayText = TextHelper.Replacer.replaceTextWithNamedArgument(displayText, "item", (matcher) -> translatableItemNameText);
        return displayText;
    }

    private static @NotNull ClickEvent makeDisplayClickEvent(@NotNull String displayUUID) {
        int expirationDurationSeconds = ChatDisplayInitializer.config.model().getExpirationDurationSeconds();
        return CommandCallbackManager
            .makeCallbackClickEvent((player) -> openDisplayGui(player, displayUUID), expirationDurationSeconds, TimeUnit.SECONDS);
    }

}

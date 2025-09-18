package io.github.sakurawald.fuji.module.initializer.echo.send_toast;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.GreedyString;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.ItemStackWrapper;
import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.service.toast_sender.ToastSender;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import java.util.Optional;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

@Document(id = 1751976160832L, value = """
    This module provides `/send-toast` command.
    To send the `text` as `toast` to a specified player.
    """)
@ColorBox(id = 1751976364671L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ Send a toast to a player.
    Issue: `/send-toast Alice --icon minecraft:golden_carrot \\<rb\\>eat this carrot`
    """)
@ColorBox(id = 1755692848834L, color = ColorBox.ColorBoxTypes.TIP, value = """
    You can send a toast with a custom icon.
    The syntax is the same as `/give` command.

    For example:
    1. `/send-toast @s --icon "minecraft:player_head[minecraft:profile=Steve]" \\<rb\\>Hello World`
    """)
public class SendToastInitializer extends ModuleInitializer {

    @CommandNode("send-toast")
    @CommandRequirement(level = 4)
    private static int $sendToast(@CommandSource ServerCommandSource source
        , ServerPlayerEntity player
        , Optional<AdvancementFrame> toastType
        , Optional<ItemStackWrapper> icon
        , GreedyString message
    ) {
        ItemStack $icon = icon
            .map(ItemStackWrapper::getItemStack)
            .orElse(Items.SLIME_BALL.getDefaultStack());
        AdvancementFrame $toastType = toastType.orElse(AdvancementFrame.CHALLENGE);
        Text title = TextHelper.getTextByValue(player, message.getValue());
        ToastSender.sendToast(player, $toastType, $icon, title);

        return CommandHelper.Return.SUCCESS;
    }
}


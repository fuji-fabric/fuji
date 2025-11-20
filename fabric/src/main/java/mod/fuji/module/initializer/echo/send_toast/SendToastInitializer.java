package mod.fuji.module.initializer.echo.send_toast;

import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandRequirement;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.core.command.argument.wrapper.impl.GreedyString;
import mod.fuji.core.command.argument.wrapper.impl.ItemStackWrapper;
import mod.fuji.core.document.annotation.ColorBox;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.service.toast_sender.ToastSender;
import mod.fuji.module.initializer.ModuleInitializer;
import java.util.Optional;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

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
    private static int $sendToast(@CommandSource CommandSourceStack source
        , ServerPlayer player
        , Optional<AdvancementType> toastType
        , Optional<ItemStackWrapper> icon
        , GreedyString message
    ) {
        ItemStack $icon = icon
            .map(ItemStackWrapper::getItemStack)
            .orElse(Items.SLIME_BALL.getDefaultInstance());
        AdvancementType $toastType = toastType.orElse(AdvancementType.CHALLENGE);
        Component title = TextHelper.getTextByValue(player, message.getValue());
        ToastSender.sendToast(player, $toastType, $icon, title);

        return CommandHelper.Return.SUCCESS;
    }
}


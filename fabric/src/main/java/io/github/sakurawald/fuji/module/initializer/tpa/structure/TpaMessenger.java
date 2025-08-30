package io.github.sakurawald.fuji.module.initializer.tpa.structure;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

public class TpaMessenger {

    public static @NotNull Text toSenderText$Description(TpaRequest tpaRequest) {
        return tpaRequest.tpahere ? TextHelper.getTextByKey(tpaRequest.getSender(), "tpa.others_to_you", PlayerHelper.getPlayerName(tpaRequest.receiver))
            : TextHelper.getTextByKey(tpaRequest.getSender(), "tpa.you_to_others", PlayerHelper.getPlayerName(tpaRequest.receiver));
    }

    public static MutableText toSenderText$Sent(TpaRequest tpaRequest) {
        Text cancelText =
            TextHelper.getTextByKey(tpaRequest.sender, "reject.button")
                .copy()
                .fillStyle(Style.EMPTY
                    .withHoverEvent(TextHelper.Events.HoverEvent.makeShowTextAction(TextHelper.getTextByKey(tpaRequest.getSender(), "cancel")))
                    .withClickEvent(TextHelper.Events.ClickEvent.makeRunCommandAction("/tpacancel %s".formatted(PlayerHelper.getPlayerName(tpaRequest.getReceiver()))))
                );

        return toSenderText$Description(tpaRequest)
            .copy()
            .append(TextHelper.TEXT_SPACE)
            .append(cancelText);
    }

    static @NotNull Text toReceiverText$Description(TpaRequest tpaRequest) {
        return tpaRequest.tpahere ? TextHelper.getTextByKey(tpaRequest.getReceiver(), "tpa.you_to_others", PlayerHelper.getPlayerName(tpaRequest.sender))
            : TextHelper.getTextByKey(tpaRequest.getReceiver(), "tpa.others_to_you", PlayerHelper.getPlayerName(tpaRequest.sender));
    }

    @NotNull
    public static MutableText toReceiverText$Sent(TpaRequest tpaRequest) {
        Text acceptText = TextHelper.getTextByKey(tpaRequest.receiver, "accept.button")
            .copy()
            .fillStyle(Style.EMPTY
                .withHoverEvent(TextHelper.Events.HoverEvent.makeShowTextAction(TextHelper.getTextByKey(tpaRequest.getReceiver(), "accept")))
                .withClickEvent(TextHelper.Events.ClickEvent.makeRunCommandAction("/tpaaccept %s".formatted(PlayerHelper.getPlayerName(tpaRequest.sender)))));

        Text denyText = TextHelper.getTextByKey(tpaRequest.receiver, "reject.button")
                .copy()
                .fillStyle(Style.EMPTY
                    .withHoverEvent(TextHelper.Events.HoverEvent.makeShowTextAction(TextHelper.getTextByKey(tpaRequest.getReceiver(), "deny")))
                    .withClickEvent(TextHelper.Events.ClickEvent.makeRunCommandAction("/tpadeny %s".formatted(PlayerHelper.getPlayerName(tpaRequest.sender))))
                );

        return toReceiverText$Description(tpaRequest)
            .copy()
            .append(TextHelper.TEXT_SPACE)
            .append(acceptText)
            .append(TextHelper.TEXT_SPACE)
            .append(denyText);
    }

    public static MutableText toSenderText$Accepted(TpaRequest tpaRequest) {
        return toSenderText$Description(tpaRequest)
            .copy()
            .append(TextHelper.TEXT_SPACE)
            .append(TextHelper.getTextByKey(tpaRequest.sender, "accept.circle"));
    }

    public static MutableText toReceiverText$Accepted(TpaRequest tpaRequest) {
        return toReceiverText$Description(tpaRequest)
            .copy()
            .append(TextHelper.TEXT_SPACE)
            .append(TextHelper.getTextByKey(tpaRequest.receiver, "accept.circle"));
    }

    public static MutableText toSenderText$Denied(TpaRequest tpaRequest) {
        return toSenderText$Description(tpaRequest)
            .copy()
            .append(TextHelper.TEXT_SPACE)
            .append(TextHelper.getTextByKey(tpaRequest.sender, "reject.circle"));
    }

    public static MutableText toReceiverText$Denied(TpaRequest tpaRequest) {
        return toReceiverText$Description(tpaRequest)
            .copy()
            .append(TextHelper.TEXT_SPACE)
            .append(TextHelper.getTextByKey(tpaRequest.receiver, "reject.circle"));
    }

    public static MutableText toSenderText$Cancelled(TpaRequest tpaRequest) {
        return toSenderText$Description(tpaRequest)
            .copy()
            .formatted(Formatting.STRIKETHROUGH);
    }

    public static MutableText toReceiverText$Cancelled(TpaRequest tpaRequest) {
        return toReceiverText$Description(tpaRequest)
            .copy()
            .formatted(Formatting.STRIKETHROUGH);
    }
}

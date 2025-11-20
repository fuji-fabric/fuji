package mod.fuji.module.initializer.tpa.structure;

import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import org.jetbrains.annotations.NotNull;

public class TpaMessenger {

    public static @NotNull Component toSenderText$Description(TpaRequest tpaRequest) {
        return tpaRequest.tpahere ? TextHelper.getTextByKey(tpaRequest.getSender(), "tpa.others_to_you", PlayerHelper.getPlayerName(tpaRequest.receiver))
            : TextHelper.getTextByKey(tpaRequest.getSender(), "tpa.you_to_others", PlayerHelper.getPlayerName(tpaRequest.receiver));
    }

    public static MutableComponent toSenderText$Sent(TpaRequest tpaRequest) {
        Component cancelText =
            TextHelper.getTextByKey(tpaRequest.sender, "reject.button")
                .copy()
                .withStyle(Style.EMPTY
                    .withHoverEvent(TextHelper.Events.HoverEvent.makeShowTextAction(TextHelper.getTextByKey(tpaRequest.getSender(), "cancel")))
                    .withClickEvent(TextHelper.Events.ClickEvent.makeRunCommandAction("/tpacancel %s".formatted(PlayerHelper.getPlayerName(tpaRequest.getReceiver()))))
                );

        return toSenderText$Description(tpaRequest)
            .copy()
            .append(TextHelper.TEXT_SPACE)
            .append(cancelText);
    }

    static @NotNull Component toReceiverText$Description(TpaRequest tpaRequest) {
        return tpaRequest.tpahere ? TextHelper.getTextByKey(tpaRequest.getReceiver(), "tpa.you_to_others", PlayerHelper.getPlayerName(tpaRequest.sender))
            : TextHelper.getTextByKey(tpaRequest.getReceiver(), "tpa.others_to_you", PlayerHelper.getPlayerName(tpaRequest.sender));
    }

    @NotNull
    public static MutableComponent toReceiverText$Sent(TpaRequest tpaRequest) {
        Component acceptText = TextHelper.getTextByKey(tpaRequest.receiver, "accept.button")
            .copy()
            .withStyle(Style.EMPTY
                .withHoverEvent(TextHelper.Events.HoverEvent.makeShowTextAction(TextHelper.getTextByKey(tpaRequest.getReceiver(), "accept")))
                .withClickEvent(TextHelper.Events.ClickEvent.makeRunCommandAction("/tpaaccept %s".formatted(PlayerHelper.getPlayerName(tpaRequest.sender)))));

        Component denyText = TextHelper.getTextByKey(tpaRequest.receiver, "reject.button")
                .copy()
                .withStyle(Style.EMPTY
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

    public static MutableComponent toSenderText$Accepted(TpaRequest tpaRequest) {
        return toSenderText$Description(tpaRequest)
            .copy()
            .append(TextHelper.TEXT_SPACE)
            .append(TextHelper.getTextByKey(tpaRequest.sender, "accept.circle"));
    }

    public static MutableComponent toReceiverText$Accepted(TpaRequest tpaRequest) {
        return toReceiverText$Description(tpaRequest)
            .copy()
            .append(TextHelper.TEXT_SPACE)
            .append(TextHelper.getTextByKey(tpaRequest.receiver, "accept.circle"));
    }

    public static MutableComponent toSenderText$Denied(TpaRequest tpaRequest) {
        return toSenderText$Description(tpaRequest)
            .copy()
            .append(TextHelper.TEXT_SPACE)
            .append(TextHelper.getTextByKey(tpaRequest.sender, "reject.circle"));
    }

    public static MutableComponent toReceiverText$Denied(TpaRequest tpaRequest) {
        return toReceiverText$Description(tpaRequest)
            .copy()
            .append(TextHelper.TEXT_SPACE)
            .append(TextHelper.getTextByKey(tpaRequest.receiver, "reject.circle"));
    }

    public static MutableComponent toSenderText$Cancelled(TpaRequest tpaRequest) {
        return toSenderText$Description(tpaRequest)
            .copy()
            .withStyle(ChatFormatting.STRIKETHROUGH);
    }

    public static MutableComponent toReceiverText$Cancelled(TpaRequest tpaRequest) {
        return toReceiverText$Description(tpaRequest)
            .copy()
            .withStyle(ChatFormatting.STRIKETHROUGH);
    }
}

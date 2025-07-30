package io.github.sakurawald.fuji.module.initializer.tpa.structure;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.module.initializer.tpa.TpaInitializer;
import lombok.Data;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

import java.util.Timer;
import java.util.TimerTask;

@Data
public class TpaRequest {

    public final ServerPlayerEntity sender;
    public final ServerPlayerEntity receiver;
    public final boolean tpahere;
    public Timer timeoutTimer;

    public TpaRequest(ServerPlayerEntity sender, ServerPlayerEntity receiver, boolean tpahere) {
        this.sender = sender;
        this.receiver = receiver;
        this.tpahere = tpahere;
    }

    public boolean isSimilarTo(@NotNull TpaRequest that) {
        return (this.sender.equals(that.sender) && this.receiver.equals(that.receiver))
            || (this.sender.equals(that.receiver) && this.receiver.equals(that.sender));
    }

    public ServerPlayerEntity getTeleportWho() {
        return tpahere ? getReceiver() : getSender();
    }

    public ServerPlayerEntity getTeleportTo() {
        return tpahere ? getSender() : getReceiver();
    }

    public void startTimeout() {
        var that = this;
        timeoutTimer = new Timer();
        timeoutTimer.schedule(
            new TimerTask() {
                @Override
                public void run() {
                    TpaInitializer.getRequests().remove(that);
                    getSender().sendMessage(toSenderText$Cancelled());
                    getReceiver().sendMessage(toReceiverText$Cancelled());
                }
            },
            TpaInitializer.config.model().request_timeout * 1000L
        );
    }

    public void cancelTimeout() {
        timeoutTimer.cancel();
    }

    private @NotNull Text toSenderText$Description() {
        return tpahere ? TextHelper.getTextByKey(getSender(), "tpa.others_to_you", PlayerHelper.getPlayerName(receiver))
            : TextHelper.getTextByKey(getSender(), "tpa.you_to_others", PlayerHelper.getPlayerName(receiver));
    }

    public MutableText toSenderText$Sent() {
        Text cancelText =
            TextHelper.getTextByKey(sender, "reject.button")
                .copy()
                .fillStyle(Style.EMPTY
                    .withHoverEvent(TextHelper.Events.HoverEvent.makeShowTextAction(TextHelper.getTextByKey(getSender(), "cancel")))
                    .withClickEvent(TextHelper.Events.ClickEvent.makeRunCommandAction("/tpacancel %s".formatted(PlayerHelper.getPlayerName(getReceiver()))))
                );

        return toSenderText$Description()
            .copy()
            .append(TextHelper.TEXT_SPACE)
            .append(cancelText);
    }

    private @NotNull Text toReceiverText$Description() {
        return tpahere ? TextHelper.getTextByKey(getReceiver(), "tpa.you_to_others", PlayerHelper.getPlayerName(sender))
            : TextHelper.getTextByKey(getReceiver(), "tpa.others_to_you", PlayerHelper.getPlayerName(sender));
    }

    @NotNull
    public MutableText toReceiverText$Sent() {
        Text acceptText = TextHelper.getTextByKey(receiver, "accept.button")
            .copy()
            .fillStyle(Style.EMPTY
                .withHoverEvent(TextHelper.Events.HoverEvent.makeShowTextAction(TextHelper.getTextByKey(getReceiver(), "accept")))
                .withClickEvent(TextHelper.Events.ClickEvent.makeRunCommandAction("/tpaaccept %s".formatted(PlayerHelper.getPlayerName(sender)))));

        Text denyText = TextHelper.getTextByKey(receiver, "reject.button")
                .copy()
                .fillStyle(Style.EMPTY
                    .withHoverEvent(TextHelper.Events.HoverEvent.makeShowTextAction(TextHelper.getTextByKey(getReceiver(), "deny")))
                    .withClickEvent(TextHelper.Events.ClickEvent.makeRunCommandAction("/tpadeny %s".formatted(PlayerHelper.getPlayerName(sender))))
                );

        return toReceiverText$Description()
            .copy()
            .append(TextHelper.TEXT_SPACE)
            .append(acceptText)
            .append(TextHelper.TEXT_SPACE)
            .append(denyText);
    }

    public MutableText toSenderText$Accepted() {
        return toSenderText$Description()
            .copy()
            .append(TextHelper.TEXT_SPACE)
            .append(TextHelper.getTextByKey(sender, "accept.circle"));
    }

    public MutableText toReceiverText$Accepted() {
        return toReceiverText$Description()
            .copy()
            .append(TextHelper.TEXT_SPACE)
            .append(TextHelper.getTextByKey(receiver, "accept.circle"));
    }

    public MutableText toSenderText$Denied() {
        return toSenderText$Description()
            .copy()
            .append(TextHelper.TEXT_SPACE)
            .append(TextHelper.getTextByKey(sender, "reject.circle"));
    }

    public MutableText toReceiverText$Denied() {
        return toReceiverText$Description()
            .copy()
            .append(TextHelper.TEXT_SPACE)
            .append(TextHelper.getTextByKey(receiver, "reject.circle"));
    }

    public MutableText toSenderText$Cancelled() {
        return toSenderText$Description()
            .copy()
            .formatted(Formatting.STRIKETHROUGH);
    }

    public MutableText toReceiverText$Cancelled() {
        return toReceiverText$Description()
            .copy()
            .formatted(Formatting.STRIKETHROUGH);
    }

}

package io.github.sakurawald.module.initializer.tpa.structure;

import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.module.initializer.tpa.TpaInitializer;
import lombok.Getter;
import lombok.ToString;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

import java.util.Timer;
import java.util.TimerTask;

@ToString
public class TpaRequest {

    private static final String CIRCLE = "●";
    private static final String TICK = "[✔]";
    private static final String CROSS = "[❌]";

    @Getter
    private final ServerPlayerEntity sender;
    @Getter
    private final ServerPlayerEntity receiver;
    @Getter
    private final boolean tpahere;
    private Timer timer;

    public TpaRequest(ServerPlayerEntity sender, ServerPlayerEntity receiver, boolean tpahere) {
        this.sender = sender;
        this.receiver = receiver;
        this.tpahere = tpahere;
    }

    public boolean similarTo(@NotNull TpaRequest other) {
        return this.sender.equals(other.sender) && this.receiver.equals(other.receiver)
            || this.sender.equals(other.receiver) && this.receiver.equals(other.sender);
    }

    public ServerPlayerEntity getTeleportWho() {
        return tpahere ? getReceiver() : getSender();
    }

    public ServerPlayerEntity getTeleportTo() {
        return tpahere ? getSender() : getReceiver();
    }

    public void startTimeout() {
        var that = this;
        timer = new Timer();
        timer.schedule(
            new TimerTask() {
                @Override
                public void run() {
                    getSender().sendMessage(asSenderText$Cancelled());
                    getReceiver().sendMessage(asReceiverText$Cancelled());
                    // don't forget to remove this request
                    TpaInitializer.getRequests().remove(that);
                }
            },
            TpaInitializer.config.model().timeout * 1000L
        );
    }

    public void cancelTimeout() {
        timer.cancel();
    }

    public @NotNull Text asSenderText$Description() {
        return tpahere ? TextHelper.getTextByKey(getSender(), "tpa.others_to_you", receiver.getGameProfile().getName())
            : TextHelper.getTextByKey(getSender(), "tpa.you_to_others", receiver.getGameProfile().getName());
    }

    public MutableText asSenderText$Sent() {
        Text cancelText =
            Text.literal(CROSS)
                .fillStyle(Style.EMPTY
                    .withFormatting(Formatting.RED)
                    .withHoverEvent(new HoverEvent.ShowText(TextHelper.getTextByKey(getSender(), "cancel")))
                    .withClickEvent(new ClickEvent.RunCommand("/tpacancel %s".formatted(getReceiver().getGameProfile().getName())))
                );

        return asSenderText$Description()
            .copy()
            .append(TextHelper.TEXT_SPACE)
            .append(cancelText);
    }

    public @NotNull Text asReceiverText$Description() {
        return tpahere ? TextHelper.getTextByKey(getReceiver(), "tpa.you_to_others", sender.getGameProfile().getName())
            : TextHelper.getTextByKey(getReceiver(), "tpa.others_to_you", sender.getGameProfile().getName());
    }

    public @NotNull MutableText asReceiverText$Sent() {
        Text acceptText = Text.literal(TICK)
            .fillStyle(Style.EMPTY
                .withFormatting(Formatting.GREEN)
                .withHoverEvent(new HoverEvent.ShowText(TextHelper.getTextByKey(getReceiver(), "accept")))
                .withClickEvent(new ClickEvent.RunCommand("/tpaaccept %s".formatted(sender.getGameProfile().getName()))));

        Text denyText =
            Text.literal(CROSS)
                .fillStyle(Style.EMPTY
                    .withFormatting(Formatting.RED)
                    .withHoverEvent(new HoverEvent.ShowText(TextHelper.getTextByKey(getReceiver(), "deny")))
                    .withClickEvent(new ClickEvent.RunCommand("/tpadeny %s".formatted(sender.getGameProfile().getName())))
                );

        return asReceiverText$Description()
            .copy()
            .append(TextHelper.TEXT_SPACE)
            .append(acceptText)
            .append(TextHelper.TEXT_SPACE)
            .append(denyText);
    }

    public MutableText asSenderText$Accepted() {
        return asSenderText$Description()
            .copy()
            .append(TextHelper.TEXT_SPACE)
            .append(Text.literal(CIRCLE).formatted(Formatting.GREEN));
    }

    public MutableText asReceiverText$Accepted() {
        return asReceiverText$Description()
            .copy()
            .append(TextHelper.TEXT_SPACE)
            .append(Text.literal(CIRCLE).formatted(Formatting.GREEN));
    }

    public MutableText asSenderText$Denied() {
        return asSenderText$Description()
            .copy()
            .append(TextHelper.TEXT_SPACE)
            .append(Text.literal(CIRCLE).formatted(Formatting.RED));
    }

    public MutableText asReceiverText$Denied() {
        return asReceiverText$Description()
            .copy()
            .append(TextHelper.TEXT_SPACE)
            .append(Text.literal(CIRCLE).formatted(Formatting.RED));
    }

    public MutableText asSenderText$Cancelled() {
        return asSenderText$Description()
            .copy()
            .formatted(Formatting.STRIKETHROUGH);
    }

    public MutableText asReceiverText$Cancelled() {
        return asReceiverText$Description()
            .copy()
            .formatted(Formatting.STRIKETHROUGH);
    }
}

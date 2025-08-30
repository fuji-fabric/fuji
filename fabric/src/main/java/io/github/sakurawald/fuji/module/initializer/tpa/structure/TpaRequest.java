package io.github.sakurawald.fuji.module.initializer.tpa.structure;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.module.initializer.tpa.TpaInitializer;
import io.github.sakurawald.fuji.module.initializer.tpa.service.TpaService;
import lombok.Data;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Timer;
import java.util.TimerTask;

@Data
public class TpaRequest {

    public final ServerPlayerEntity sender;
    public final ServerPlayerEntity receiver;
    public final boolean tpahere;
    public Timer timeoutTimer;

    public TpaRequest(@NotNull ServerPlayerEntity sender, @NotNull ServerPlayerEntity receiver, boolean tpahere) {
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
                    TpaService.getRequests().remove(that);
                    TextHelper.sendMessageByText(getSender(), TpaMessenger.toSenderText$Cancelled(TpaRequest.this));
                    TextHelper.sendMessageByText(getReceiver(), TpaMessenger.toReceiverText$Cancelled(TpaRequest.this));
                }
            },
            TpaInitializer.config.model().getRequestTimeout() * 1000L
        );
    }

    public void cancelTimeout() {
        timeoutTimer.cancel();
    }

}

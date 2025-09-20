package io.github.sakurawald.fuji.module.initializer.tpa.service;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.job.impl.PlaySoundJob;
import io.github.sakurawald.fuji.core.structure.GlobalPos;
import io.github.sakurawald.fuji.module.initializer.tpa.TpaInitializer;
import io.github.sakurawald.fuji.module.initializer.tpa.structure.ResponseStatus;
import io.github.sakurawald.fuji.module.initializer.tpa.structure.TpaMessenger;
import io.github.sakurawald.fuji.module.initializer.tpa.structure.TpaRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Getter;
import net.minecraft.server.network.ServerPlayerEntity;

public class TpaService {
    @Getter
    private static final List<TpaRequest> requests = new ArrayList<>();

    public static int doRequest(ServerPlayerEntity source, ServerPlayerEntity target, boolean tpahere) {
        if (target.isRemoved()) {
            TextHelper.sendTextByKey(source, "player.invalid", PlayerHelper.getPlayerName(target));
            return CommandHelper.Return.FAILURE;
        }

        /* Make a new request. */
        TpaRequest request = new TpaRequest(source, target, tpahere);

        // Should not send a request to self.
        if (request.getSender().equals(request.getReceiver())) {
            TextHelper.sendTextByKey(request.getSender(), "tpa.request_to_self");
            return CommandHelper.Return.FAILURE;
        }

        // Should not have existed similar request.
        if (requests.stream().anyMatch(request::isSimilarTo)) {
            TextHelper.sendTextByKey(request.getSender(), "tpa.similar_request_exists");
            return CommandHelper.Return.FAILURE;
        }

        /* Submit the request. */
        requests.add(request);
        request.startTimeout();
        PlaySoundJob.scheduleJob(TpaInitializer.config.model().getMentionPlayer(), request.getReceiver());

        /* Send feedback messages. */
        TextHelper.sendMessageByText(request.getReceiver(), TpaMessenger.toReceiverText$Sent(request));
        TextHelper.sendMessageByText(request.getSender(), TpaMessenger.toSenderText$Sent(request));
        return CommandHelper.Return.SUCCESS;
    }

    public static int doResponse(ServerPlayerEntity player, ServerPlayerEntity target, ResponseStatus status) {
        /* Find relative request. */
        Optional<TpaRequest> requestOpt = requests
            .stream()
            .filter(request ->
                status == ResponseStatus.CANCEL ?
                    (request.getSender().equals(player) && request.getReceiver().equals(target))
                    : (request.getSender().equals(target) && request.getReceiver().equals(player)))
            .findFirst();
        if (requestOpt.isEmpty()) {
            TextHelper.sendTextByKey(player, "tpa.no_relative_ticket");
            return CommandHelper.Return.FAILURE;
        }

        /* Send feedback messages. */
        TpaRequest request = requestOpt.get();
        if (status == ResponseStatus.ACCEPT) {
            ServerPlayerEntity who = request.getTeleportWho();
            ServerPlayerEntity to = request.getTeleportTo();
            PlaySoundJob.scheduleJob(TpaInitializer.config.model().getMentionPlayer(), request.isTpahere() ? to : who);
            new GlobalPos(PlayerHelper.getServerWorld(to), to.getX(), to.getY(), to.getZ(), to.getYaw(), to.getPitch())
                .teleport(who);

            TextHelper.sendText(request.getSender(), TpaMessenger.toSenderText$Accepted(request), TextHelper.Sender.TextLocation.ACTION_BAR);
            TextHelper.sendMessageByText(request.getReceiver(), TpaMessenger.toReceiverText$Accepted(request));
        } else if (status == ResponseStatus.DENY) {
            TextHelper.sendText(request.getSender(), TpaMessenger.toSenderText$Denied(request), TextHelper.Sender.TextLocation.ACTION_BAR);
            TextHelper.sendMessageByText(request.getReceiver(), TpaMessenger.toReceiverText$Denied(request));
        } else if (status == ResponseStatus.CANCEL) {
            TextHelper.sendMessageByText(request.getSender(), TpaMessenger.toSenderText$Cancelled(request));
            TextHelper.sendMessageByText(request.getReceiver(), TpaMessenger.toReceiverText$Cancelled(request));
        }

        /* Invalidate the request. */
        request.cancelTimeout();
        requests.remove(request);
        return CommandHelper.Return.SUCCESS;
    }

    public static int doResponseToAll(ServerPlayerEntity me, ResponseStatus responseStatus) {
        // Filter the target players.
        ArrayList<TpaRequest> targetPlayers = requests
            .stream()
            .filter(request -> {
                if (responseStatus == ResponseStatus.CANCEL) {
                    return request.getSender().equals(me);
                }

                return request.getReceiver().equals(me);
            })
            .collect(Collectors.toCollection(ArrayList::new));

        // Iterate it.
        targetPlayers.forEach(request -> {
            ServerPlayerEntity responseTarget;
            if (responseStatus == ResponseStatus.CANCEL) {
                responseTarget = request.getReceiver();
            } else {
                responseTarget = request.getSender();
            }

            doResponse(me, responseTarget, responseStatus);
        });

        return CommandHelper.Return.SUCCESS;
    }
}

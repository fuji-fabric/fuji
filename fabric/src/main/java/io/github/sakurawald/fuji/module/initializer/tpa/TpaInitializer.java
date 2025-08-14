package io.github.sakurawald.fuji.module.initializer.tpa;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.job.impl.PlaySoundJob;
import io.github.sakurawald.fuji.core.structure.GlobalPos;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.tpa.config.model.TpaConfigModel;
import io.github.sakurawald.fuji.module.initializer.tpa.structure.TpaRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Getter;
import net.minecraft.server.network.ServerPlayerEntity;


@Document(id = 1751826540953L, value = "This module provides `/tpa` and `/tpahere` commands.")
public class TpaInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<TpaConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, TpaConfigModel.class);

    @Getter
    private static final List<TpaRequest> requests = new ArrayList<>();

    @CommandNode("tpa")
    private static int $tpa(@CommandSource ServerPlayerEntity player, ServerPlayerEntity target) {
        return doRequest(player, target, false);
    }

    @CommandNode("tpahere")
    private static int $tpahere(@CommandSource ServerPlayerEntity player, ServerPlayerEntity target) {
        return doRequest(player, target, true);
    }

    @CommandNode("tpaaccept")
    private static int $tpaaccept(@CommandSource ServerPlayerEntity player, ServerPlayerEntity target) {
        return doResponse(player, target, ResponseStatus.ACCEPT);
    }

    private static int doResponseToAll(ServerPlayerEntity me, ResponseStatus responseStatus) {
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

    @CommandNode("tpaaccept all")
    private static int $tpaaccept(@CommandSource ServerPlayerEntity player) {
        return doResponseToAll(player, ResponseStatus.ACCEPT);
    }

    @CommandNode("tpadeny")
    private static int $tpadeny(@CommandSource ServerPlayerEntity player, ServerPlayerEntity target) {
        return doResponse(player, target, ResponseStatus.DENY);
    }

    @CommandNode("tpadeny all")
    private static int $tpadeny(@CommandSource ServerPlayerEntity player) {
        return doResponseToAll(player, ResponseStatus.DENY);
    }

    @CommandNode("tpacancel")
    private static int $tpacancel(@CommandSource ServerPlayerEntity player, ServerPlayerEntity target) {
        return doResponse(player, target, ResponseStatus.CANCEL);
    }

    @CommandNode("tpacancel all")
    private static int $tpacancel(@CommandSource ServerPlayerEntity player) {
        return doResponseToAll(player, ResponseStatus.CANCEL);
    }

    private static int doResponse(ServerPlayerEntity player, ServerPlayerEntity target, ResponseStatus status) {
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
            PlaySoundJob.scheduleJob(config.model().mention_player, request.isTpahere() ? to : who);
            new GlobalPos(to.getWorld(), to.getX(), to.getY(), to.getZ(), to.getYaw(), to.getPitch())
                .teleport(who);

            TextHelper.sendText(request.getSender(), request.toSenderText$Accepted(), TextHelper.Sender.TextLocation.ACTION_BAR);
            TextHelper.sendMessageByText(request.getReceiver(), request.toReceiverText$Accepted());
        } else if (status == ResponseStatus.DENY) {
            TextHelper.sendText(request.getSender(), request.toSenderText$Denied(), TextHelper.Sender.TextLocation.ACTION_BAR);
            TextHelper.sendMessageByText(request.getReceiver(), request.toReceiverText$Denied());
        } else if (status == ResponseStatus.CANCEL) {
            TextHelper.sendMessageByText(request.getSender(), request.toSenderText$Cancelled());
            TextHelper.sendMessageByText(request.getReceiver(), request.toReceiverText$Cancelled());
        }

        /* Invalidate the request. */
        request.cancelTimeout();
        requests.remove(request);
        return CommandHelper.Return.SUCCESS;
    }

    private static int doRequest(ServerPlayerEntity source, ServerPlayerEntity target, boolean tpahere) {
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
        PlaySoundJob.scheduleJob(config.model().mention_player, request.getReceiver());

        /* Send feedback messages. */
        TextHelper.sendMessageByText(request.getReceiver(), request.toReceiverText$Sent());
        TextHelper.sendMessageByText(request.getSender(), request.toSenderText$Sent());
        return CommandHelper.Return.SUCCESS;
    }

    // NOTE: Maybe the `CANCEL` should not be a response status.
    private enum ResponseStatus {ACCEPT, DENY, CANCEL}
}

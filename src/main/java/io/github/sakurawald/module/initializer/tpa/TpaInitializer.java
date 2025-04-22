package io.github.sakurawald.module.initializer.tpa;

import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.command.annotation.CommandNode;
import io.github.sakurawald.core.command.annotation.CommandSource;
import io.github.sakurawald.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.core.job.impl.MentionPlayersJob;
import io.github.sakurawald.core.structure.SpatialPose;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import io.github.sakurawald.module.initializer.tpa.config.model.TpaConfigModel;
import io.github.sakurawald.module.initializer.tpa.structure.TpaRequest;
import lombok.Getter;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


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

    private static void doResponseToAll(ServerPlayerEntity me, ResponseStatus responseStatus) {
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
    }

    @CommandNode("tpaaccept all")
    private static int $tpaaccept(@CommandSource ServerPlayerEntity player) {
        doResponseToAll(player, ResponseStatus.ACCEPT);
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("tpadeny")
    private static int $tpadeny(@CommandSource ServerPlayerEntity player, ServerPlayerEntity target) {
        return doResponse(player, target, ResponseStatus.DENY);
    }

    @CommandNode("tpadeny all")
    private static int $tpadeny(@CommandSource ServerPlayerEntity player) {
        doResponseToAll(player, ResponseStatus.DENY);
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("tpacancel")
    private static int $tpacancel(@CommandSource ServerPlayerEntity player, ServerPlayerEntity target) {
        return doResponse(player, target, ResponseStatus.CANCEL);
    }

    @CommandNode("tpacancel all")
    private static int $tpacancel(@CommandSource ServerPlayerEntity player) {
        doResponseToAll(player, ResponseStatus.CANCEL);
        return CommandHelper.Return.SUCCESS;
    }

    private static int doResponse(ServerPlayerEntity player, ServerPlayerEntity target, ResponseStatus status) {
        /* resolve relative request */
        Optional<TpaRequest> requestOptional = requests.stream()
            .filter(request ->
                status == ResponseStatus.CANCEL ?
                    (request.getSender().equals(player) && request.getReceiver().equals(target))
                    : (request.getSender().equals(target) && request.getReceiver().equals(player)))
            .findFirst();
        if (requestOptional.isEmpty()) {
            TextHelper.sendActionBarByKey(player, "tpa.no_relative_ticket");
            return CommandHelper.Return.FAIL;
        }

        TpaRequest request = requestOptional.get();
        if (status == ResponseStatus.ACCEPT) {
            request.getSender().sendMessage(request.asSenderText$Accepted(), true);

            ServerPlayerEntity who = request.getTeleportWho();
            ServerPlayerEntity to = request.getTeleportTo();
            MentionPlayersJob.requestJob(config.model().mention_player, request.isTpahere() ? to : who);

            new SpatialPose(to.getWorld(), to.getX(), to.getY(), to.getZ(), to.getYaw(), to.getPitch())
                .teleport(who);
        } else if (status == ResponseStatus.DENY) {
            request.getSender().sendMessage(request.asSenderText$Denied(), true);
            request.getReceiver().sendMessage(request.asReceiverText$Denied());
        } else if (status == ResponseStatus.CANCEL) {
            request.getSender().sendMessage(request.asSenderText$Cancelled());
            request.getReceiver().sendMessage(request.asReceiverText$Cancelled());
        }

        request.cancelTimeout();
        requests.remove(request);
        return CommandHelper.Return.SUCCESS;
    }

    private static int doRequest(ServerPlayerEntity source, ServerPlayerEntity target, boolean tpahere) {
        /* add request */
        TpaRequest request = new TpaRequest(source, target, tpahere);

        /* has similar request ? */
        if (request.getSender().equals(request.getReceiver())) {
            TextHelper.sendActionBarByKey(request.getSender(), "tpa.request_to_self");

            return CommandHelper.Return.FAIL;
        }

        if (requests.stream().anyMatch(request::similarTo)) {
            TextHelper.sendActionBarByKey(request.getSender(), "tpa.similar_request_exists");
            return CommandHelper.Return.FAIL;
        }

        requests.add(request);
        request.startTimeout();

        /* feedback */
        request.getReceiver().sendMessage(request.asReceiverText$Sent());
        MentionPlayersJob.requestJob(config.model().mention_player, request.getReceiver());
        request.getSender().sendMessage(request.asSenderText$Sent());
        return CommandHelper.Return.SUCCESS;
    }

    private enum ResponseStatus {ACCEPT, DENY, CANCEL}
}

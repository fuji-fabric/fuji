package mod.fuji.core.service.bossbar;

import mod.fuji.core.annotation.Unused;
import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.event.annotation.EventConsumer;
import mod.fuji.core.event.message.player.PlayerDamageEvent;
import mod.fuji.core.event.message.server.tick.ServerTickStartEvent;
import mod.fuji.core.service.bossbar.structure.InterruptibleTicket;
import java.util.Optional;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class BossBarManager {

    private static final List<BossBarTicket> tickets = new CopyOnWriteArrayList<>();
    private static final List<BossBarTicket> addedTickets = new CopyOnWriteArrayList<>();

    @EventConsumer
    private static void processOnPlayerDamagedEvent(PlayerDamageEvent event) {
        ServerPlayer player = event.getPlayer();
        tickets
            .stream()
            .filter(it -> it instanceof InterruptibleTicket interruptibleTicket
                && interruptibleTicket.getInterruptible().isEnable()
                && interruptibleTicket.getInterruptible().isInterruptOnDamaged()
                // the spawn mechanism of fake-player is different, they are spawned in overworld, and then teleport to target position.
                && PlayerHelper.Kind.isRealPlayer(player)
                && it.getPlayers().stream().anyMatch(p -> p.equals(player)))
            .forEach(it -> it.setAborted(true));
    }


    public static Collection<BossBarTicket> getTickets() {
        return Collections.unmodifiableCollection(tickets);
    }

    public static void addTicket(BossBarTicket ticket) {
        addedTickets.add(ticket);
    }

    private static void abortTicket(@NotNull BossBarTicket ticket) {
        ticket.clearPlayers();
        tickets.remove(ticket);
    }

    @EventConsumer
    private static void tickBossBarTickets(@Unused ServerTickStartEvent event) {
        /* add tickets */
        tickets.addAll(addedTickets);
        addedTickets.clear();

        /* iterate tickets */
        if (tickets.isEmpty()) return;

        List<BossBarTicket> abortedTickets = new ArrayList<>();
        List<BossBarTicket> completedTickets = new ArrayList<>();

        for (BossBarTicket ticket : tickets) {
            // is aborted ?
            if (ticket.isAborted()) {
                abortedTickets.add(ticket);
                continue;
            }

            if (!ticket.preProgressChange()) {
                ticket.setAborted(true);
                continue;
            }

            // ensure visibility
            ticket.getBossBar().setVisible(true);

            for (ServerPlayer player : ticket.getPlayers()) {
                if (player.hasDisconnected()) {
                    ticket.onPlayerDisconnected(player);
                    ticket.removePlayer(player);
                }
            }

            // fix: bossbar.progress() may be greater than 1.0F and throw an IllegalArgumentException.
            try {
                ticket.step();
            } catch (Exception e) {
                /*
                 The exception will be thrown, if
                 1. One of the viewer of the bossbar is disconnected (but not removed from the bossbar).
                 2. The viewers of the bossbar is empty.
                 */
                ticket.setAborted(true);
                return;
            }

            if (!ticket.postProgressChange()) {
                ticket.setAborted(true);
                continue;
            }

            // even the ServerPlayer is disconnected, the bossbar will still be ticked.
            if (ticket.isCompleted()) {
                // set completed tickets to abort, so that it will be removed form the list.
                ticket.setAborted(true);
                completedTickets.add(ticket);
            }
        }

        /* process tickets */
        completedTickets.forEach(BossBarTicket::onComplete);
        abortedTickets.forEach(BossBarManager::abortTicket);
    }

    @SuppressWarnings("unchecked")
    public static <T extends BossBarTicket> Optional<T> findBossbarTicket(@NotNull Class<T> ticketType, @NotNull ServerPlayer player) {
        return getTickets()
            .stream()
            .filter(it -> {
                return it.getPlayers().stream().anyMatch(p -> p.equals(player))
                    && ticketType.isInstance(it);
            })
            .map(it -> (T) it)
            .findFirst();
    }

}

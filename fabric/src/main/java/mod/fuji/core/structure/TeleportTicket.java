package mod.fuji.core.structure;

import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.service.bossbar.structure.Interruptible;
import mod.fuji.core.service.bossbar.structure.InterruptibleTicket;
import lombok.Getter;
import net.minecraft.world.BossEvent;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.world.entity.Relative;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.Set;

@Getter
public class TeleportTicket extends InterruptibleTicket {

    private final GlobalPos destination;
    private final Set<Relative> flags;

    private TeleportTicket(@NotNull ServerPlayer player
        , GlobalPos source
        , GlobalPos destination
        , float progress
        , int totalMs
        , Interruptible interruptible
        , Set<Relative> flags
    ) {
        super(
            new ServerBossEvent(TextHelper.getTextByKey(player, "teleport_warmup.bossbar.name"), BossEvent.BossBarColor.BLUE, net.minecraft.world.BossEvent.BossBarOverlay.PROGRESS)
            , totalMs
            , player
            , source
            , interruptible);

        this.destination = destination;
        this.flags = flags;

        // set progress
        this.getBossBar().setProgress(progress);
    }

    public static @NotNull TeleportTicket make(@NotNull ServerPlayer player, GlobalPos source, GlobalPos destination, int totalMs, Interruptible interruptible, Set<Relative> flags) {
        return new TeleportTicket(player, source, destination, 0f, totalMs, interruptible, flags);
    }

    public static @NotNull TeleportTicket makeVipTicket(@NotNull ServerPlayer player, GlobalPos source, GlobalPos destination) {
        return new TeleportTicket(player, source, destination, 1f, 2048, Interruptible.makeUninterruptible(), EnumSet.noneOf(Relative.class));
    }

    @Override
    protected void onComplete() {
        destination.teleport(player, flags);
    }

}

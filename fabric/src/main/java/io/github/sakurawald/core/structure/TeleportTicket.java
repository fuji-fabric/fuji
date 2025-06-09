package io.github.sakurawald.core.structure;

import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.manager.impl.bossbar.structure.Interruptible;
import io.github.sakurawald.core.manager.impl.bossbar.structure.InterruptibleTicket;
import lombok.Getter;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.Set;

@Getter
public class TeleportTicket extends InterruptibleTicket {

    private final SpatialPose destination;
    private final Set<PositionFlag> flags;

    private TeleportTicket(@NotNull ServerPlayerEntity player
        , SpatialPose source
        , SpatialPose destination
        , float progress
        , int totalMs
        , Interruptible interruptible
        , Set<PositionFlag> flags
    ) {
        super(
            new ServerBossBar(TextHelper.getTextByKey(player, "teleport_warmup.bossbar.name"), BossBar.Color.BLUE, net.minecraft.entity.boss.BossBar.Style.PROGRESS)
            , totalMs
            , player
            , source
            , interruptible);

        this.destination = destination;
        this.flags = flags;
        // set progress
        this.getBossBar().setPercent(progress);
    }

    public static @NotNull TeleportTicket make(@NotNull ServerPlayerEntity player, SpatialPose source, SpatialPose destination, int totalMs, Interruptible interruptible, Set<PositionFlag> flags) {
        return new TeleportTicket(player, source, destination, 0f, totalMs, interruptible, flags);
    }

    public static @NotNull TeleportTicket makeVipTicket(@NotNull ServerPlayerEntity player, SpatialPose source, SpatialPose destination) {
        return new TeleportTicket(player, source, destination, 1f, 2048, Interruptible.makeUninterruptible(), EnumSet.noneOf(PositionFlag.class));
    }

    @Override
    protected void onComplete() {
        destination.teleport(player, flags);
    }

}

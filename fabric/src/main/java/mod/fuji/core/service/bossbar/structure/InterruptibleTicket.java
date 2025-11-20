package mod.fuji.core.service.bossbar.structure;

import mod.fuji.core.auxiliary.minecraft.EntityHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.auxiliary.minecraft.WorldHelper;
import mod.fuji.core.extension.PlayerCombatExtension;
import mod.fuji.core.service.bossbar.BossBarTicket;
import mod.fuji.core.structure.GlobalPos;
import lombok.Getter;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public abstract class InterruptibleTicket extends BossBarTicket {
    protected final @NotNull ServerPlayer player;
    protected final @NotNull GlobalPos source;
    protected final @NotNull Interruptible interruptible;

    public InterruptibleTicket(
            ServerBossEvent bossBar
        , int totalMS
        , @NotNull ServerPlayer player
        , @NotNull GlobalPos source
        , @NotNull Interruptible interruptible
    ) {
        super(bossBar, totalMS, List.of(player));
        this.player = player;
        this.source = source;
        this.interruptible = interruptible;
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override
    protected boolean preProgressChange() {
        // ignore
        if (!this.interruptible.isEnable()) {
            return true;
        }

        // check in combat
        if (this.interruptible.isInterruptInCombat() && ((PlayerCombatExtension) player).fuji$inCombat()) {
            TextHelper.sendTextByKey(player, "teleport_warmup.in_combat");
            return false;
        }

        // check distance
        double interruptDistance = this.getInterruptible().getInterruptDistance();
        double distanceSquare = WorldHelper.squareDistance(EntityHelper.getPos(player), this.source.getX(), this.source.getY(), this.source.getZ());
        if (distanceSquare >= (interruptDistance * interruptDistance)) {
            return false;
        }

        return true;
    }
}

package mod.fuji.core.manager.impl.bossbar.structure;

import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.extension.PlayerCombatExtension;
import mod.fuji.core.manager.impl.bossbar.BossBarTicket;
import mod.fuji.core.structure.GlobalPos;
import lombok.Getter;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public abstract class InterruptibleTicket extends BossBarTicket {
    protected final @NotNull ServerPlayerEntity player;
    protected final @NotNull GlobalPos source;
    protected final @NotNull Interruptible interruptible;

    public InterruptibleTicket(
        ServerBossBar bossBar
        , int totalMS
        , @NotNull ServerPlayerEntity player
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
        if (player.getPos().squaredDistanceTo(this.source.getX(), this.source.getY(), this.source.getZ()) >= (interruptDistance * interruptDistance)) {
            return false;
        }

        return true;
    }
}

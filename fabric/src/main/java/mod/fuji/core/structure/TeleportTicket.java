package mod.fuji.core.structure;

import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.service.bossbar.structure.Interruptible;
import mod.fuji.core.service.bossbar.structure.InterruptibleTicket;
import lombok.Getter;
import net.minecraft.world.BossEvent;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

@Getter
public class TeleportTicket extends InterruptibleTicket {

    private final GlobalPos destination;
    private final RelativeFlagsIR flags;

    private TeleportTicket(@NotNull ServerPlayer player
        , GlobalPos source
        , GlobalPos destination
        , float progress
        , int totalMs
        , Interruptible interruptible
        , RelativeFlagsIR flags
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

    public static @NotNull TeleportTicket make(@NotNull ServerPlayer player, GlobalPos source, GlobalPos destination, int totalMs, Interruptible interruptible, RelativeFlagsIR flags) {
        return new TeleportTicket(player, source, destination, 0f, totalMs, interruptible, flags);
    }

    public static @NotNull TeleportTicket makeVipTicket(@NotNull ServerPlayer player, GlobalPos source, GlobalPos destination) {
        var absolute = RelativeFlagsIR.empty();
        return new TeleportTicket(player, source, destination, 1f, 2048, Interruptible.makeUninterruptible(), absolute);
    }

    @Override
    protected void onComplete() {
        destination.teleport(player, flags);
    }

}

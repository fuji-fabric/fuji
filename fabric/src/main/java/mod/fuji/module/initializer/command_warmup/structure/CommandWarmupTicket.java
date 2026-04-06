package mod.fuji.module.initializer.command_warmup.structure;

import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.command.executor.CommandExecutor;
import mod.fuji.core.command.executor.structure.ExtendedCommandSource;
import mod.fuji.core.service.bossbar.BossBarManager;
import mod.fuji.core.service.bossbar.structure.InterruptibleTicket;
import mod.fuji.core.structure.GlobalPos;
import lombok.Getter;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

@Getter
public class CommandWarmupTicket extends InterruptibleTicket {

    private final String commandString;

    private CommandWarmupTicket(@NotNull ServerPlayer player, @NotNull String commandString, CommandWarmupNode entry) {
        super(makeCommandWarmupBossEvent(player, commandString)
            , entry.getCommand().getWarmupTimeMs()
            , player
            , GlobalPos.of(player)
            , entry.getInterruptible());

        this.commandString = commandString;
    }

    private static @NotNull ServerBossEvent makeCommandWarmupBossEvent(@NotNull ServerPlayer player, @NotNull String commandString) {
        Component textByKey = TextHelper.getTextByKey(player, "command_warmup.bossbar.name", commandString);

        return BossBarManager.makeServerBossEvent(textByKey, net.minecraft.world.BossEvent.BossBarColor.GREEN , net.minecraft.world.BossEvent.BossBarOverlay.PROGRESS);
    }

    public static CommandWarmupTicket make(ServerPlayer player, String commandString, CommandWarmupNode setup) {
        return new CommandWarmupTicket(player, commandString, setup);
    }

    @Override
    protected void onComplete() {
        CommandExecutor.executeSingle(ExtendedCommandSource.asPlayer(player.createCommandSourceStack(), player), commandString);
    }

}

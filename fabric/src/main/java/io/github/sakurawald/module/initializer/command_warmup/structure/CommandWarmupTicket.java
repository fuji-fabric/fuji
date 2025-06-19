package io.github.sakurawald.module.initializer.command_warmup.structure;

import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.manager.impl.bossbar.structure.InterruptibleTicket;
import io.github.sakurawald.core.structure.SpatialPose;
import lombok.Getter;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

@Getter
public class CommandWarmupTicket extends InterruptibleTicket {

    private final String commandString;

    private CommandWarmupTicket(@NotNull ServerPlayerEntity player, @NotNull String commandString, CommandWarmupNode entry) {
        super(new ServerBossBar(TextHelper.getTextByKey(player, "command_warmup.bossbar.name", commandString), net.minecraft.entity.boss.BossBar.Color.GREEN, net.minecraft.entity.boss.BossBar.Style.PROGRESS)
            , entry.getCommand().getMs()
            , player
            , SpatialPose.of(player)
            , entry.getInterruptible());

        this.commandString = commandString;
    }

    public static CommandWarmupTicket make(ServerPlayerEntity player, String commandString, CommandWarmupNode setup) {
        return new CommandWarmupTicket(player, commandString, setup);
    }

    @Override
    protected void onComplete() {
        CommandHelper.executeCommand(player, commandString);
    }

}

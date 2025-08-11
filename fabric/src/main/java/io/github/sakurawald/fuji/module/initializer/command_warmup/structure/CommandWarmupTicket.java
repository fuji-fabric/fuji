package io.github.sakurawald.fuji.module.initializer.command_warmup.structure;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.executor.CommandExecutor;
import io.github.sakurawald.fuji.core.command.executor.structure.ExtendedCommandSource;
import io.github.sakurawald.fuji.core.manager.impl.bossbar.structure.InterruptibleTicket;
import io.github.sakurawald.fuji.core.structure.GlobalPos;
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
            , GlobalPos.of(player)
            , entry.getInterruptible());

        this.commandString = commandString;
    }

    public static CommandWarmupTicket make(ServerPlayerEntity player, String commandString, CommandWarmupNode setup) {
        return new CommandWarmupTicket(player, commandString, setup);
    }

    @Override
    protected void onComplete() {
        CommandExecutor.execute(ExtendedCommandSource.asPlayer(player.getCommandSource(), player), commandString);
    }

}

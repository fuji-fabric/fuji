package io.github.sakurawald.module.initializer.echo.send_bossbar;

import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.command.annotation.CommandNode;
import io.github.sakurawald.core.command.annotation.CommandRequirement;
import io.github.sakurawald.core.command.annotation.CommandSource;
import io.github.sakurawald.core.command.argument.wrapper.StepType;
import io.github.sakurawald.core.command.argument.wrapper.impl.GreedyString;
import io.github.sakurawald.core.command.argument.wrapper.impl.StringList;
import io.github.sakurawald.core.command.executor.CommandExecutor;
import io.github.sakurawald.core.command.structure.ExtendedCommandSource;
import io.github.sakurawald.core.manager.Managers;
import io.github.sakurawald.core.manager.impl.bossbar.BossBarTicket;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import io.github.sakurawald.module.initializer.echo.send_bossbar.structure.SendBossbarTicket;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Collections;
import java.util.Optional;

@CommandRequirement(level = 4)
public class SendBossbarInitializer extends ModuleInitializer {

    @CommandNode("send-bossbar")
    private static int sendBossbar(@CommandSource ServerCommandSource source
        , ServerPlayerEntity player
        , Optional<Integer> totalMs
        , Optional<BossBar.Color> color
        , Optional<BossBar.Style> style
        , Optional<StringList> commandList
        , Optional<Boolean> notifyMeOnComplete
        , Optional<StepType> stepType
        , GreedyString title) {

        /* extract props */
        Integer $totalMs = totalMs.orElse(3000);
        BossBar.Color $color = color.orElse(BossBar.Color.PURPLE);
        BossBar.Style $style = style.orElse(BossBar.Style.PROGRESS);
        StringList $commandList = commandList.orElse(new StringList(Collections.emptyList()));
        Boolean $notifyMeOnComplete = notifyMeOnComplete.orElse(false);
        StepType $stepType = stepType.orElse(StepType.FORWARD);

        /* construct the ticket*/
        BossBarTicket bossBarTicket = new SendBossbarTicket(title.getValue(), $color, $style, $totalMs, $stepType, player, () -> {
            ExtendedCommandSource extendedCommandSource = ExtendedCommandSource.asConsole(player.getCommandSource());
            CommandExecutor.execute(extendedCommandSource, $commandList.getValue());

            // notify
            if ($notifyMeOnComplete) {
                TextHelper.sendMessageByKey(source, "echo.send_bossbar.notify", player.getGameProfile().getName(), $commandList.getValue());
            }
        });
        Managers.getBossBarManager().addTicket(bossBarTicket);

        return CommandHelper.Return.SUCCESS;
    }

}

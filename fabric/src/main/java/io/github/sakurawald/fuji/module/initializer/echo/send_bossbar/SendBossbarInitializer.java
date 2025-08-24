package io.github.sakurawald.fuji.module.initializer.echo.send_bossbar;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.manager.impl.bossbar.command.argument.wrapper.StepType;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.GreedyString;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.StringList;
import io.github.sakurawald.fuji.core.command.executor.CommandExecutor;
import io.github.sakurawald.fuji.core.command.executor.structure.ExtendedCommandSource;
import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.manager.Managers;
import io.github.sakurawald.fuji.core.manager.impl.bossbar.BossBarTicket;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.echo.send_bossbar.structure.SendBossbarTicket;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Collections;
import java.util.Optional;

@Document(id = 1751976322975L, value = """
    This module provides `/send-bossbar` command.
    To send a `text` as the `bossbar` to a specified player.
    """)
@ColorBox(id = 1751976574472L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ A simple example.
    Issue: `/send-bossbar Alice Hello World`

    ◉ All in one example.
    Issue: `/send-bossbar Alice --stepType BACKWARD --totalMs 5000 --color PURPLE --style NOTCHED_6 --notifyMeOnComplete true --commandList "say the player %player:name% is healed|heal others %player:name%" \\<rb\\>Healing is coming [elapsed_time]/[total_time]/[left_time]`
    """)
public class SendBossbarInitializer extends ModuleInitializer {

    @CommandNode("send-bossbar")
    @CommandRequirement(level = 4)
    private static int $sendBossbar(@CommandSource ServerCommandSource source
        , ServerPlayerEntity player
        , Optional<Integer> totalMs
        , Optional<BossBar.Color> color
        , Optional<BossBar.Style> style
        , Optional<StringList> commandList
        , Optional<Boolean> notifyMeOnComplete
        , Optional<StepType> stepType
        , GreedyString title) {

        /* Resolve variables. */
        Integer $totalMs = totalMs.orElse(3000);
        BossBar.Color $color = color.orElse(BossBar.Color.PURPLE);
        BossBar.Style $style = style.orElse(BossBar.Style.PROGRESS);
        StringList $commandList = commandList.orElse(new StringList(Collections.emptyList()));
        Boolean $notifyMeOnComplete = notifyMeOnComplete.orElse(false);
        StepType $stepType = stepType.orElse(StepType.FORWARD);

        /* Make the bossbar ticket. */
        BossBarTicket bossBarTicket = new SendBossbarTicket(title.getValue(), $color, $style, $totalMs, $stepType, player, () -> {
            ExtendedCommandSource extendedCommandSource = ExtendedCommandSource.asConsole(player.getCommandSource());
            CommandExecutor.executeBatch(extendedCommandSource, $commandList.getValue());

            if ($notifyMeOnComplete) {
                TextHelper.sendTextByKey(source, "echo.send_bossbar.notify", player.getGameProfile().getName(), $commandList.getValue());
            }
        });

        /* Submit the ticket. */
        Managers.getBossBarManager().addTicket(bossBarTicket);

        return CommandHelper.Return.SUCCESS;
    }

}

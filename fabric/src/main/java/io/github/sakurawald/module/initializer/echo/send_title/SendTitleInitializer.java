package io.github.sakurawald.module.initializer.echo.send_title;

import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.command.annotation.CommandNode;
import io.github.sakurawald.core.command.annotation.CommandRequirement;
import io.github.sakurawald.core.command.annotation.CommandSource;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Optional;

public class SendTitleInitializer extends ModuleInitializer {

    @CommandNode("send-title")
    @CommandRequirement(level = 4)
    private static int sendTitle(@CommandSource ServerCommandSource source, ServerPlayerEntity player
        , Optional<String> mainTitle
        , Optional<String> subTitle
        , Optional<Integer> fadeInTicks
        , Optional<Integer> stayTicks
        , Optional<Integer> fadeOutTicks
    ) {

        String $mainTitle = mainTitle.orElse("main title");
        String $subTitle = subTitle.orElse("");
        int $fadeInTicks = fadeInTicks.orElse(10);
        int $stayTicks = stayTicks.orElse(70);
        int $fadeOutTicks = fadeOutTicks.orElse(20);

        Text mainTitleComponent = TextHelper.getTextByValue(player, $mainTitle);
        Text subTitleComponent = TextHelper.getTextByValue(player, $subTitle);

        player.networkHandler.sendPacket(new TitleFadeS2CPacket($fadeInTicks, $stayTicks, $fadeOutTicks));
        player.networkHandler.sendPacket(new TitleS2CPacket(mainTitleComponent));
        player.networkHandler.sendPacket(new SubtitleS2CPacket(subTitleComponent));

        return CommandHelper.Return.SUCCESS;
    }
}

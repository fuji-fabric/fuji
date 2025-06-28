package io.github.sakurawald.fuji.module.initializer.echo.send_title;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
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

        Text mainTitleText = TextHelper.getTextByValue(player, $mainTitle);
        Text subTitleText = TextHelper.getTextByValue(player, $subTitle);

        TextHelper.sendTitle(player, $fadeInTicks, $stayTicks, $fadeOutTicks, mainTitleText, subTitleText);
        return CommandHelper.Return.SUCCESS;
    }

}

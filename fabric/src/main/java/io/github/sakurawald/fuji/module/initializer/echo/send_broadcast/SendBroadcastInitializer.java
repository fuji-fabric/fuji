package io.github.sakurawald.fuji.module.initializer.echo.send_broadcast;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.GreedyString;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import net.minecraft.text.Text;

public class SendBroadcastInitializer extends ModuleInitializer {

    @CommandNode("send-broadcast")
    @CommandRequirement(level = 4)
    private static int sendBroadcast(GreedyString rest) {
        String message = rest.getValue();

        Text broadcastText = TextHelper.getTextByValue(null, message);
        TextHelper.sendBroadcastByValue(broadcastText);
        return CommandHelper.Return.SUCCESS;
    }

}

package io.github.sakurawald.fuji.module.initializer.echo.send_broadcast;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.GreedyString;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import net.minecraft.text.Text;

@Document(id = 1751975894810L, value = """
    This module provides the `/send-broadcast` command.
    To send the `text` as `message` to `all online players`.
    """)
public class SendBroadcastInitializer extends ModuleInitializer {

    @CommandNode("send-broadcast")
    @CommandRequirement(level = 4)
    private static int sendBroadcast(GreedyString rest) {
        String message = rest.getValue();

        Text broadcastText = TextHelper.getTextByValue(null, message);
        TextHelper.sendBroadcastByText(broadcastText);
        return CommandHelper.Return.SUCCESS;
    }

}

package mod.fuji.module.initializer.echo.send_broadcast;

import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandRequirement;
import mod.fuji.core.command.argument.wrapper.impl.GreedyString;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.module.initializer.ModuleInitializer;
import net.minecraft.text.Text;

@Document(id = 1751975894810L, value = """
    This module provides the `/send-broadcast` command.
    To send the `text` as `message` to `all online players`.
    """)
public class SendBroadcastInitializer extends ModuleInitializer {

    @CommandNode("send-broadcast")
    @CommandRequirement(level = 4)
    private static int $sendBroadcast(GreedyString rest) {
        String message = rest.getValue();

        Text broadcastText = TextHelper.getTextByValue(null, message);
        TextHelper.sendBroadcastByText(broadcastText);
        return CommandHelper.Return.SUCCESS;
    }

}

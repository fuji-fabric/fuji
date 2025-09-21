package mod.fuji.module.initializer.echo.send_console;

import mod.fuji.core.auxiliary.LogUtil;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandRequirement;
import mod.fuji.core.command.argument.wrapper.impl.GreedyString;
import mod.fuji.core.document.annotation.ColorBox;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.module.initializer.ModuleInitializer;

@Document(id = 1751976790532L, value = """
    This module provides the `/send-console` command.
    To send a string into the `console`.
    """)
@ColorBox(id = 1751976813043L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ Send a string to the console.
    Issue: `/send-console Hello World`

    ◉ Log an action to the console.
    You can integrate with other modules, to `log a specific action`.
    The effect is like: `/run as console send-console Player %player:name% joins the server.`
    """)
public class SendConsoleInitializer extends ModuleInitializer {

    @CommandNode("send-console")
    @CommandRequirement(level = 4)
    private static int $sendConsole(GreedyString message) {
        LogUtil.info("{}", message.getValue());
        return CommandHelper.Return.SUCCESS;
    }

}


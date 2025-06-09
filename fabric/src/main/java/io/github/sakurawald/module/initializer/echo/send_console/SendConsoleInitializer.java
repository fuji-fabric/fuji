package io.github.sakurawald.module.initializer.echo.send_console;

import io.github.sakurawald.core.auxiliary.LogUtil;
import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.core.command.annotation.CommandNode;
import io.github.sakurawald.core.command.annotation.CommandRequirement;
import io.github.sakurawald.core.command.argument.wrapper.impl.GreedyString;
import io.github.sakurawald.module.initializer.ModuleInitializer;

public class SendConsoleInitializer extends ModuleInitializer {

    @CommandNode("send-console")
    @CommandRequirement(level = 4)
    private static int sendConsole(GreedyString message) {
        LogUtil.info("Send Console: {}", message.getValue());
        return CommandHelper.Return.SUCCESS;
    }

}


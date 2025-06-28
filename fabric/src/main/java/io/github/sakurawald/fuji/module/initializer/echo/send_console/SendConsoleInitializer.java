package io.github.sakurawald.fuji.module.initializer.echo.send_console;

import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.GreedyString;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;

public class SendConsoleInitializer extends ModuleInitializer {

    @CommandNode("send-console")
    @CommandRequirement(level = 4)
    private static int sendConsole(GreedyString message) {
        LogUtil.info("Send Console: {}", message.getValue());
        return CommandHelper.Return.SUCCESS;
    }

}


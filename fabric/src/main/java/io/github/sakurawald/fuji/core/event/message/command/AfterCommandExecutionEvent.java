package io.github.sakurawald.fuji.core.event.message.command;

import java.util.Optional;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class AfterCommandExecutionEvent extends AbstractCommandExecutionEvent {

    public AfterCommandExecutionEvent(@NotNull Object commandExecutor, @NotNull ServerCommandSource commandSource, @NotNull String commandString, @NotNull Optional<CallbackInfo> callback, @NotNull Optional<Integer> commandReturnValue) {
        super(commandExecutor, commandSource, commandString, callback, commandReturnValue);
    }
}

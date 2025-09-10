package io.github.sakurawald.fuji.core.event.message.command;

import java.util.Optional;
import lombok.ToString;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@ToString(callSuper = true)
public class BeforeCommandExecutionEvent extends AbstractCommandExecutionEvent {

    public BeforeCommandExecutionEvent(@NotNull Object commandExecutor, @NotNull ServerCommandSource commandSource, @NotNull String commandString, @NotNull Optional<CallbackInfo> callback, @NotNull Optional<Integer> commandReturnValue) {
        super(commandExecutor, commandSource, commandString, callback, commandReturnValue);
    }
}

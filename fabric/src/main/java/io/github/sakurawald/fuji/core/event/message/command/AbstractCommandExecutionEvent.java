package io.github.sakurawald.fuji.core.event.message.command;

import io.github.sakurawald.fuji.core.event.message.BaseEvent;
import java.util.Optional;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@EqualsAndHashCode(callSuper = true)
@Data
public class AbstractCommandExecutionEvent extends BaseEvent {

    @NotNull Object commandExecutor;
    @NotNull ServerCommandSource commandSource;
    @NotNull String commandString;

    @NotNull Optional<CallbackInfo> callback;
    @NotNull Optional<Integer> commandReturnValue;
}

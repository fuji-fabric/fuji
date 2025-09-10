package io.github.sakurawald.fuji.core.event.message.command;

import io.github.sakurawald.fuji.core.event.message.BaseEvent;
import java.util.Optional;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Data
@EqualsAndHashCode(callSuper = true)
public class CommandExecutionPreEvent extends BaseEvent {

    @NotNull Object commandExecutor;
    @NotNull ServerCommandSource commandSource;
    @NotNull String commandString;
    @NotNull Optional<CallbackInfo> callback;
    @NotNull Optional<Integer> commandReturnValue;

}

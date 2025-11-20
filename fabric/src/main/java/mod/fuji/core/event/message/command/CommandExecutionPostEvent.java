package mod.fuji.core.event.message.command;

import mod.fuji.core.event.message.BaseEvent;
import java.util.Optional;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Data
@EqualsAndHashCode(callSuper = true)
public class CommandExecutionPostEvent extends BaseEvent {

    @NotNull Object commandExecutor;
    @NotNull CommandSourceStack commandSource;
    @NotNull String commandString;
    @NotNull Optional<CallbackInfo> callback;
    @NotNull Optional<Integer> commandReturnValue;

}

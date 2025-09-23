package mod.fuji.core.command.descriptor;

import java.lang.reflect.Method;
import java.util.List;
import mod.fuji.core.command.argument.structure.CommandArgument;
import org.jetbrains.annotations.NotNull;

public class StandardCommandDescriptor extends CommandDescriptor {
    public StandardCommandDescriptor(@NotNull Method method, @NotNull List<CommandArgument> commandArguments) {
        super(method, commandArguments);
    }

    @Override
    public boolean isConsoleSpammer() {
        return false;
    }
}

package mod.fuji.core.command.argument.adapter.impl;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mod.fuji.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import mod.fuji.core.command.argument.structure.CommandArgument;
import lombok.SneakyThrows;
import net.minecraft.commands.arguments.item.FunctionArgument;
import net.minecraft.commands.CommandSourceStack;

import java.util.List;
import org.jetbrains.annotations.NotNull;

public class CommandFunctionArgumentTypeAdapter extends BaseArgumentTypeAdapter {
    @Override
    protected ArgumentType<?> makeArgumentType() {
        return FunctionArgument.functions();
    }

    @SneakyThrows(CommandSyntaxException.class)
    @Override
    protected Object makeArgumentValue(@NotNull CommandContext<CommandSourceStack> context, @NotNull CommandArgument commandArgument) {
        return FunctionArgument.getFunctions(context, commandArgument.getArgumentName());
    }

    @Override
    public List<Class<?>> getTypeClasses() {
        return List.of(
            #if MC_VER <= MC_1_20_2
            net.minecraft.commands.CommandFunction.class
            #elif MC_VER > MC_1_20_2
            net.minecraft.commands.functions.CommandFunction.class
            #endif
        );
    }

    @Override
    public List<String> getTypeNames() {
        return List.of("command-function");
    }
}

package mod.fuji.core.command.argument.adapter.impl;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import mod.fuji.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import mod.fuji.core.command.argument.structure.CommandArgument;
import mod.fuji.core.command.argument.wrapper.impl.NotSupportedType;
import net.minecraft.commands.arguments.RangeArgument;
import net.minecraft.commands.CommandSourceStack;

import java.util.List;
import org.jetbrains.annotations.NotNull;

public class IntRangeArgumentTypeAdapter extends BaseArgumentTypeAdapter {
    @Override
    protected ArgumentType<?> makeArgumentType() {
        return RangeArgument.intRange();
    }

    @Override
    protected Object makeArgumentValue(@NotNull CommandContext<CommandSourceStack> context, @NotNull CommandArgument commandArgument) {
        return RangeArgument.Ints.getRange(context, commandArgument.getArgumentName());
    }

    @Override
    public List<Class<?>> getTypeClasses() {
        return List.of(NotSupportedType.class);
    }

    @Override
    public List<String> getTypeNames() {
        return List.of("int-range");
    }
}

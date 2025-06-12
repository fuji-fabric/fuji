package io.github.sakurawald.core.command.argument.adapter.impl;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import io.github.sakurawald.core.command.argument.structure.Argument;
import net.minecraft.command.argument.NumberRangeArgumentType;
import net.minecraft.predicate.NumberRange;
import net.minecraft.server.command.ServerCommandSource;

import java.util.List;

public class FloatRangeArgumentTypeAdapter extends BaseArgumentTypeAdapter {
    @Override
    protected ArgumentType<?> makeArgumentType() {
        return NumberRangeArgumentType.floatRange();
    }

    @Override
    protected Object makeArgumentObject(CommandContext<ServerCommandSource> context, Argument argument) {
        return NumberRangeArgumentType.FloatRangeArgumentType.getRangeArgument(context, argument.getArgumentName());
    }

    @Override
    public List<Class<?>> getTypeClasses() {
        #if MC_VER <= MC_1_20_1
        return List.of(NumberRange.FloatRange.class);
        #elif MC_VER > MC_1_20_1
        return List.of(NumberRange.DoubleRange.class);
        #endif
    }

    @Override
    public List<String> getTypeStrings() {
        return List.of("float-range");
    }
}

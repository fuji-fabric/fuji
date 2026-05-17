package mod.fuji.core.command.argument.adapter.impl;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.util.Arrays;
import java.util.List;
import mod.fuji.core.auxiliary.StringUtil;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import mod.fuji.core.command.argument.structure.CommandArgument;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.NotNull;

public class DyeColorArgumentTypeAdapter extends BaseArgumentTypeAdapter {
    @Override
    public List<Class<?>> getTypeClasses() {
        return List.of(DyeColor.class);
    }

    @Override
    public List<String> getTypeNames() {
        return List.of("dye-color");
    }

    @Override
    protected ArgumentType<?> makeArgumentType() {
        return StringArgumentType.string();
    }

    @Override
    protected Object makeArgumentValue(@NotNull CommandContext<CommandSourceStack> context, @NotNull CommandArgument commandArgument) {
        String argumentValue = StringArgumentType.getString(context, commandArgument.getArgumentName());
        return DyeColor.valueOf(argumentValue);
    }

    @Override
    protected @NotNull RequiredArgumentBuilder<CommandSourceStack, ?> makeRequiredArgumentBuilder(@NotNull String argumentName) {
        return super.makeRequiredArgumentBuilder(argumentName)
            .suggests(CommandHelper.Suggestion.iterable(() -> Arrays
                .stream(DyeColor.values())
                .map(it -> StringUtil.toUpperCase(it.getName()))
                .toList()));
    }
}

package mod.fuji.core.command.argument.adapter.impl;


import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.WorldHelper;
import mod.fuji.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import mod.fuji.core.command.argument.structure.CommandArgument;
import java.util.ArrayList;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;

import java.util.List;
import org.jetbrains.annotations.NotNull;

public class BlockPosArgumentTypeAdapter extends BaseArgumentTypeAdapter {

    @Override
    protected ArgumentType<?> makeArgumentType() {
        return BlockPosArgument.blockPos();
    }

    @Override
    public Object makeArgumentValue(@NotNull CommandContext<CommandSourceStack> context, @NotNull CommandArgument commandArgument) {
        return BlockPosArgument.getBlockPos(context, commandArgument.getArgumentName());
    }

    @SuppressWarnings("CodeBlock2Expr")
    @Override
    protected @NotNull RequiredArgumentBuilder<CommandSourceStack, ?> makeRequiredArgumentBuilder(@NotNull String argumentName) {
        return super
            .makeRequiredArgumentBuilder(argumentName)
            .suggests(CommandHelper.Suggestion.iterable((context, builder) -> {
                List<String> suggestions = new ArrayList<>();

                CommandHelper.Source.withServerPlayerEntity(context, player -> {
                    WorldHelper.Raycast
                        .getLookingAtBlock(player)
                        .ifPresent(lookingAtBlockPos -> {
                            suggestions.add(WorldHelper.Formatter.format(lookingAtBlockPos));
                        });

                });

                return suggestions;
            }));
    }

    @Override
    public List<Class<?>> getTypeClasses() {
        return List.of(BlockPos.class);
    }

    @Override
    public List<String> getTypeNames() {
        return List.of("blockpos", "block-pos");
    }
}

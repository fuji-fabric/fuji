package io.github.sakurawald.fuji.core.command.argument.adapter.impl;


import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.WorldHelper;
import io.github.sakurawald.fuji.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import io.github.sakurawald.fuji.core.command.argument.structure.CommandArgument;
import java.util.ArrayList;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import org.jetbrains.annotations.NotNull;

public class BlockPosArgumentTypeAdapter extends BaseArgumentTypeAdapter {

    @Override
    protected ArgumentType<?> makeArgumentType() {
        return BlockPosArgumentType.blockPos();
    }

    @Override
    public Object makeArgumentValue(@NotNull CommandContext<ServerCommandSource> context, @NotNull CommandArgument commandArgument) {
        return BlockPosArgumentType.getBlockPos(context, commandArgument.getArgumentName());
    }

    @SuppressWarnings("CodeBlock2Expr")
    @Override
    protected @NotNull RequiredArgumentBuilder<ServerCommandSource, ?> makeRequiredArgumentBuilder(@NotNull String argumentName) {
        return super
            .makeRequiredArgumentBuilder(argumentName)
            .suggests(CommandHelper.Suggestion.iterable((context, builder) -> {
                List<String> suggestions = new ArrayList<>(List.of("~", "~ ~", "~ ~ ~"));

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

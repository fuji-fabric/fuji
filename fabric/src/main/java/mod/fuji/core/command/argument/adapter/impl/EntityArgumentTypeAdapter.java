package mod.fuji.core.command.argument.adapter.impl;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.WorldHelper;
import mod.fuji.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import mod.fuji.core.command.argument.structure.CommandArgument;
import java.util.ArrayList;
import lombok.SneakyThrows;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.world.entity.Entity;
import net.minecraft.commands.CommandSourceStack;

import java.util.List;
import org.jetbrains.annotations.NotNull;

public class EntityArgumentTypeAdapter extends BaseArgumentTypeAdapter {

    @Override
    protected ArgumentType<?> makeArgumentType() {
        return EntityArgument.entity();
    }

    @SneakyThrows(CommandSyntaxException.class)
    @Override
    public Object makeArgumentValue(@NotNull CommandContext<CommandSourceStack> context, @NotNull CommandArgument commandArgument) {
        return EntityArgument.getEntity(context, commandArgument.getArgumentName());
    }

    @SuppressWarnings("CodeBlock2Expr")
    @Override
    protected @NotNull RequiredArgumentBuilder<CommandSourceStack, ?> makeRequiredArgumentBuilder(@NotNull String argumentName) {
        return super.makeRequiredArgumentBuilder(argumentName)
            .suggests(CommandHelper.Suggestion.iterable((context, builder) -> {
                List<String> suggestions = new ArrayList<>(List.of());

                CommandHelper.Source
                    .withServerPlayerEntity(context, player -> {
                        WorldHelper.Raycast
                            .getLookingAtEntity(player)
                            .ifPresent(lookingAtEntity -> {
                                suggestions.add(lookingAtEntity.getStringUUID());
                            });
                    });

                return suggestions;
            }))
            ;
    }

    @Override
    public List<Class<?>> getTypeClasses() {
        return List.of(Entity.class);
    }

    @Override
    public List<String> getTypeNames() {
        return List.of("entity");
    }
}

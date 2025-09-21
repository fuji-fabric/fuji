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
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;

import java.util.List;
import org.jetbrains.annotations.NotNull;

public class EntityArgumentTypeAdapter extends BaseArgumentTypeAdapter {

    @Override
    protected ArgumentType<?> makeArgumentType() {
        return EntityArgumentType.entity();
    }

    @SneakyThrows(CommandSyntaxException.class)
    @Override
    public Object makeArgumentValue(@NotNull CommandContext<ServerCommandSource> context, @NotNull CommandArgument commandArgument) {
        return EntityArgumentType.getEntity(context, commandArgument.getArgumentName());
    }

    @SuppressWarnings("CodeBlock2Expr")
    @Override
    protected @NotNull RequiredArgumentBuilder<ServerCommandSource, ?> makeRequiredArgumentBuilder(@NotNull String argumentName) {
        return super.makeRequiredArgumentBuilder(argumentName)
            .suggests(CommandHelper.Suggestion.iterable((context, builder) -> {
                List<String> suggestions = new ArrayList<>(List.of());

                CommandHelper.Source
                    .withServerPlayerEntity(context, player -> {
                        WorldHelper.Raycast
                            .getLookingAtEntity(player)
                            .ifPresent(lookingAtEntity -> {
                                suggestions.add(lookingAtEntity.getUuidAsString());
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

package io.github.sakurawald.fuji.module.initializer.world.manager.command.argument.adapter;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import io.github.sakurawald.fuji.core.command.argument.structure.Argument;
import io.github.sakurawald.fuji.module.initializer.world.manager.command.argument.wrapper.UnloadedRuntimeDimensionDescriptor;
import io.github.sakurawald.fuji.module.initializer.world.manager.service.WorldService;
import io.github.sakurawald.fuji.module.initializer.world.manager.structure.RuntimeDimensionDescriptor;
import java.util.List;
import java.util.Optional;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;

public class UnloadedRuntimeDimensionDescriptorArgumentTypeAdapter extends BaseArgumentTypeAdapter {
    @Override
    protected ArgumentType<?> makeArgumentType() {
        return IdentifierArgumentType.identifier();
    }

    @Override
    protected Object makeArgumentObject(CommandContext<ServerCommandSource> context, Argument argument) {
        Identifier identifier = IdentifierArgumentType.getIdentifier(context, argument.getArgumentName());
        Optional<RuntimeDimensionDescriptor> runtimeDimensionDescriptor = WorldService.getRuntimeDimensionDescriptor(identifier.toString());
        return new UnloadedRuntimeDimensionDescriptor(runtimeDimensionDescriptor.get());
    }

    @Override
    public List<Class<?>> getTypeClasses() {
        return List.of(UnloadedRuntimeDimensionDescriptor.class);
    }

    @Override
    public List<String> getTypeStrings() {
        return List.of("unloaded-runtime-dimension-descriptor");
    }

    @Override
    public RequiredArgumentBuilder<ServerCommandSource, ?> makeRequiredArgumentBuilder(String argumentName) {
        return super
            .makeRequiredArgumentBuilder(argumentName)
            .suggests(CommandHelper.Suggestion.iterable(() -> WorldService
                .getUnloadedRuntimeDimensionDescriptors()
                .stream()
                .map(RuntimeDimensionDescriptor::getDimension)
                .toList()));
    }
}

package mod.fuji.module.initializer.world.manager.command.argument.adapter;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import mod.fuji.core.command.argument.structure.CommandArgument;
import mod.fuji.module.initializer.world.manager.command.argument.wrapper.LoadedRuntimeDimensionDescriptor;
import mod.fuji.module.initializer.world.manager.service.WorldService;
import mod.fuji.module.initializer.world.manager.structure.RuntimeDimensionDescriptor;
import java.util.List;
import java.util.Optional;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class LoadedRuntimeDimensionDescriptorArgumentTypeAdapter extends BaseArgumentTypeAdapter {
    @Override
    protected ArgumentType<?> makeArgumentType() {
        return IdentifierArgumentType.identifier();
    }

    @Override
    protected Object makeArgumentValue(@NotNull CommandContext<ServerCommandSource> context, @NotNull CommandArgument commandArgument) {
        Identifier identifier = IdentifierArgumentType.getIdentifier(context, commandArgument.getArgumentName());
        Optional<RuntimeDimensionDescriptor> runtimeDimensionDescriptor = WorldService.getRuntimeDimensionDescriptor(identifier.toString());
        RuntimeDimensionDescriptor value = runtimeDimensionDescriptor.get();
        if (value.isDimensionLoaded()) {
            return new LoadedRuntimeDimensionDescriptor(value);
        }

        throw new IllegalArgumentException("The dimension is already un-loaded.");
    }

    @Override
    public List<Class<?>> getTypeClasses() {
        return List.of(LoadedRuntimeDimensionDescriptor.class);
    }

    @Override
    public List<String> getTypeNames() {
        return List.of("loaded-runtime-dimension-descriptor");
    }

    @Override
    @NotNull
    protected RequiredArgumentBuilder<ServerCommandSource, ?> makeRequiredArgumentBuilder(@NotNull String argumentName) {
        return super
            .makeRequiredArgumentBuilder(argumentName)
            .suggests(CommandHelper.Suggestion.iterable(() -> WorldService
                .getRuntimeDimensionDescriptors()
                .stream()
                .filter(RuntimeDimensionDescriptor::isDimensionLoaded)
                .map(RuntimeDimensionDescriptor::getDimension)
                .toList()));
    }
}

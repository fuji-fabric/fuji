package mod.fuji.module.initializer.world.manager.command.argument.adapter;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import mod.fuji.core.command.argument.representation.IdentifierArgumentTypeIR;
import mod.fuji.core.command.argument.structure.CommandArgument;
import mod.fuji.core.command.exception.AbortCommandExecutionException;
import mod.fuji.module.initializer.world.manager.command.argument.wrapper.UnloadedRuntimeDimensionDescriptor;
import mod.fuji.module.initializer.world.manager.service.WorldService;
import mod.fuji.module.initializer.world.manager.structure.RuntimeDimensionDescriptor;
import java.util.List;
import java.util.Optional;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.NotNull;

public class UnloadedRuntimeDimensionDescriptorArgumentTypeAdapter extends BaseArgumentTypeAdapter {
    @Override
    protected ArgumentType<?> makeArgumentType() {
        return IdentifierArgumentTypeIR.makeArgumentType();
    }

    @Override
    protected Object makeArgumentValue(@NotNull CommandContext<CommandSourceStack> context, @NotNull CommandArgument commandArgument) {
        var identifier = IdentifierArgumentTypeIR.makeArgumentValue(context, commandArgument);
        Optional<RuntimeDimensionDescriptor> runtimeDimensionDescriptor = WorldService.getRuntimeDimensionDescriptor(identifier.toString());

        return runtimeDimensionDescriptor
            .map($runtimeDimensionDescriptor -> {
                if (!$runtimeDimensionDescriptor.isDimensionLoaded()) {
                    return new UnloadedRuntimeDimensionDescriptor($runtimeDimensionDescriptor);
                }
                TextHelper.sendTextByKey(context.getSource(), "world.dimension.load.already", identifier);
                throw new AbortCommandExecutionException();
            })
            .orElseThrow(() -> {
                TextHelper.sendTextByKey(context.getSource(), "world.dimension.dimension_descriptor_not_found", identifier);
                return new AbortCommandExecutionException();
            });
    }

    @Override
    public List<Class<?>> getTypeClasses() {
        return List.of(UnloadedRuntimeDimensionDescriptor.class);
    }

    @Override
    public List<String> getTypeNames() {
        return List.of("unloaded-runtime-dimension-descriptor");
    }

    @Override
    @NotNull
    protected RequiredArgumentBuilder<CommandSourceStack, ?> makeRequiredArgumentBuilder(@NotNull String argumentName) {
        return super
            .makeRequiredArgumentBuilder(argumentName)
            .suggests(CommandHelper.Suggestion.iterable(() -> WorldService
                .getRuntimeDimensionDescriptors()
                .stream()
                .filter(it -> !it.isDimensionLoaded())
                .map(RuntimeDimensionDescriptor::getDimension)
                .toList()));
    }
}

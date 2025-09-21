package mod.fuji.module.initializer.command_state.command.argument.adapter;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import mod.fuji.core.command.argument.structure.CommandArgument;
import mod.fuji.core.command.exception.AbortCommandExecutionException;
import mod.fuji.module.initializer.command_state.service.CommandStateService;
import mod.fuji.module.initializer.command_state.structure.StateDescriptor;
import java.util.List;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.NotNull;

public class StateDescriptorArgumentTypeAdapter extends BaseArgumentTypeAdapter {
    @Override
    public List<Class<?>> getTypeClasses() {
        return List.of(StateDescriptor.class);
    }

    @Override
    public List<String> getTypeNames() {
        return List.of("state-id");
    }

    @Override
    protected ArgumentType<?> makeArgumentType() {
        return StringArgumentType.string();
    }

    @Override
    protected Object makeArgumentValue(@NotNull CommandContext<ServerCommandSource> context, @NotNull CommandArgument commandArgument) {
        String stateId = StringArgumentType.getString(context, commandArgument.getArgumentName());
        return CommandStateService
            .findStateDescriptor(stateId)
            .orElseThrow(() -> {
                TextHelper.sendTextByKey(context.getSource(), "command_state.state.not_found", stateId);
                return new AbortCommandExecutionException();
            });
    }

    @Override
    protected @NotNull RequiredArgumentBuilder<ServerCommandSource, ?> makeRequiredArgumentBuilder(@NotNull String argumentName) {
        return super.makeRequiredArgumentBuilder(argumentName)
            .suggests(CommandHelper.Suggestion.iterable(CommandStateService::listStateIds));
    }
}

package mod.fuji.module.initializer.command_cooldown.command.argument.adapter;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import mod.fuji.core.command.argument.structure.CommandArgument;
import mod.fuji.core.command.exception.AbortCommandExecutionException;
import mod.fuji.module.initializer.command_cooldown.service.NamedCooldownService;
import mod.fuji.module.initializer.command_cooldown.structure.NamedCooldownDescriptor;
import net.minecraft.commands.CommandSourceStack;

import java.util.List;
import org.jetbrains.annotations.NotNull;

public class NamedCooldownDescriptorArgumentTypeAdapter extends BaseArgumentTypeAdapter {

    @Override
    protected ArgumentType<?> makeArgumentType() {
        return StringArgumentType.string();
    }

    @Override
    protected Object makeArgumentValue(@NotNull CommandContext<CommandSourceStack> context, @NotNull CommandArgument commandArgument) {
        String id = StringArgumentType.getString(context, commandArgument.getArgumentName());
        return NamedCooldownService
            .findNamedCooldownDescriptor(id)
            .orElseThrow(() -> {
                TextHelper.sendTextByKey(context.getSource(), "command_cooldown.not_found", id);
                return new AbortCommandExecutionException();
            });
    }

    @Override
    @NotNull
    protected RequiredArgumentBuilder<CommandSourceStack, ?> makeRequiredArgumentBuilder(@NotNull String argumentName) {
        return super.makeRequiredArgumentBuilder(argumentName)
            .suggests(CommandHelper.Suggestion.iterable(() -> NamedCooldownService.getNamedCooldownDescriptors().keySet()));
    }

    @Override
    public List<Class<?>> getTypeClasses() {
        return List.of(NamedCooldownDescriptor.class);
    }

    @Override
    public List<String> getTypeNames() {
        return List.of("named-command-cooldown");
    }
}

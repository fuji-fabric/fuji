package mod.fuji.module.initializer.sign_editor.command.argument.adapter;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.util.List;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import mod.fuji.core.command.argument.structure.CommandArgument;
import mod.fuji.module.initializer.sign_editor.command.argument.wrapper.SignLine;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.NotNull;

public class SignLineArgumentTypeAdapter extends BaseArgumentTypeAdapter {
    @Override
    public List<Class<?>> getTypeClasses() {
        return List.of(SignLine.class);
    }

    @Override
    public List<String> getTypeNames() {
        return List.of("sign-line");
    }

    @Override
    protected ArgumentType<?> makeArgumentType() {
        return IntegerArgumentType.integer();
    }

    @Override
    protected Object makeArgumentValue(@NotNull CommandContext<CommandSourceStack> context, @NotNull CommandArgument commandArgument) {
        int value = IntegerArgumentType.getInteger(context, commandArgument.getArgumentName());
        return new SignLine(value);
    }

    @Override
    protected @NotNull RequiredArgumentBuilder<CommandSourceStack, ?> makeRequiredArgumentBuilder(@NotNull String argumentName) {
        return super
            .makeRequiredArgumentBuilder(argumentName)
            .suggests(CommandHelper.Suggestion.iterable(() -> List.of(1, 2, 3, 4)));
    }
}

package mod.fuji.module.initializer.echo.send_toast.command.argument.adapter;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import mod.fuji.core.command.argument.structure.CommandArgument;
import mod.fuji.core.structure.AdvancementFrameTypeWrapper;
import net.minecraft.commands.CommandSourceStack;

import java.util.List;
import org.jetbrains.annotations.NotNull;

public class AdvancementFrameArgumentTypeAdapter extends BaseArgumentTypeAdapter {

    @Override
    protected ArgumentType<?> makeArgumentType() {
        return StringArgumentType.string();
    }

    @Override
    public Object makeArgumentValue(@NotNull CommandContext<CommandSourceStack> context, @NotNull CommandArgument commandArgument) {
        String string = StringArgumentType.getString(context, commandArgument.getArgumentName());
        return AdvancementFrameTypeWrapper.valueOf(string);
    }

    @Override
    public List<Class<?>> getTypeClasses() {
        return List.of(AdvancementFrameTypeWrapper.class);
    }

    @Override
    public List<String> getTypeNames() {
        return List.of("advancement-frame");
    }

    @Override
    @NotNull
    protected RequiredArgumentBuilder<CommandSourceStack, ?> makeRequiredArgumentBuilder(@NotNull String argumentName) {
        return super
            .makeRequiredArgumentBuilder(argumentName)
            .suggests(CommandHelper.Suggestion.enums(AdvancementFrameTypeWrapper::values));
    }
}

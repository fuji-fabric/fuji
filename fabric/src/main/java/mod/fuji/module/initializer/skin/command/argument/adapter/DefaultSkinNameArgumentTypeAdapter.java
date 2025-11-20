package mod.fuji.module.initializer.skin.command.argument.adapter;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import mod.fuji.core.command.argument.structure.CommandArgument;
import mod.fuji.module.initializer.skin.command.argument.wrapper.DefaultSkinName;
import mod.fuji.module.initializer.skin.service.SkinService;
import mod.fuji.module.initializer.skin.structure.SkinDescriptor;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.NotNull;

public class DefaultSkinNameArgumentTypeAdapter extends BaseArgumentTypeAdapter {

    @Override
    protected ArgumentType<?> makeArgumentType() {
        return StringArgumentType.string();
    }

    @Override
    protected Object makeArgumentValue(@NotNull CommandContext<CommandSourceStack> context, @NotNull CommandArgument commandArgument) {
        return new DefaultSkinName(StringArgumentType.getString(context, commandArgument.getArgumentName()));
    }

    @Override
    public List<Class<?>> getTypeClasses() {
        return List.of(DefaultSkinName.class);
    }

    @Override
    public List<String> getTypeNames() {
        return List.of("default-skin-name");
    }

    @Override
    @NotNull
    protected RequiredArgumentBuilder<CommandSourceStack, ?> makeRequiredArgumentBuilder(@NotNull String argumentName) {
        return super.makeRequiredArgumentBuilder(argumentName)
            .suggests(CommandHelper.Suggestion.iterable(() -> SkinService.getDefaultSkinList()
                .stream()
                .map(SkinDescriptor::getSkinName)
                .toList()));
    }
}

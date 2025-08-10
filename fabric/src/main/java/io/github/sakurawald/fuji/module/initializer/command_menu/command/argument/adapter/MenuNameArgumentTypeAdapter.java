package io.github.sakurawald.fuji.module.initializer.command_menu.command.argument.adapter;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import io.github.sakurawald.fuji.core.command.argument.structure.CommandArgument;
import io.github.sakurawald.fuji.module.initializer.command_menu.CommandMenuInitializer;
import io.github.sakurawald.fuji.module.initializer.command_menu.command.argument.wrapper.MenuName;
import net.minecraft.server.command.ServerCommandSource;

import java.util.List;
import org.jetbrains.annotations.NotNull;

public class MenuNameArgumentTypeAdapter extends BaseArgumentTypeAdapter {
    @Override
    protected ArgumentType<?> makeArgumentType() {
        return StringArgumentType.string();
    }

    @Override
    protected Object makeArgumentValue(@NotNull CommandContext<ServerCommandSource> context, @NotNull CommandArgument commandArgument) {
        return new MenuName(StringArgumentType.getString(context, commandArgument.getArgumentName()));
    }

    @Override
    public @NotNull RequiredArgumentBuilder<ServerCommandSource, ?> makeRequiredArgumentBuilder(@NotNull String argumentName) {
        return super.makeRequiredArgumentBuilder(argumentName)
            .suggests(CommandHelper.Suggestion.iterable(() -> CommandMenuInitializer.menus.model().menus.keySet()));
    }

    @Override
    public List<Class<?>> getTypeClasses() {
        return List.of(MenuName.class);
    }

    @Override
    public List<String> getTypeNames() {
        return List.of("menu-name");
    }
}

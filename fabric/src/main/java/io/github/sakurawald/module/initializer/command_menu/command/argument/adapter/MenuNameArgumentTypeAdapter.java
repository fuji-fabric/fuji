package io.github.sakurawald.module.initializer.command_menu.command.argument.adapter;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import io.github.sakurawald.core.command.argument.structure.Argument;
import io.github.sakurawald.module.initializer.command_menu.CommandMenuInitializer;
import io.github.sakurawald.module.initializer.command_menu.command.argument.wrapper.MenuName;
import net.minecraft.server.command.ServerCommandSource;

import java.util.List;

public class MenuNameArgumentTypeAdapter extends BaseArgumentTypeAdapter {
    @Override
    protected ArgumentType<?> makeArgumentType() {
        return StringArgumentType.string();
    }

    @Override
    protected Object makeArgumentObject(CommandContext<ServerCommandSource> context, Argument argument) {
        return new MenuName(StringArgumentType.getString(context, argument.getArgumentName()));
    }

    @Override
    public RequiredArgumentBuilder<ServerCommandSource, ?> makeRequiredArgumentBuilder(String argumentName) {
        return super.makeRequiredArgumentBuilder(argumentName)
            .suggests(CommandHelper.Suggestion.iterable(() -> CommandMenuInitializer.menus.model().menus.keySet()));
    }

    @Override
    public List<Class<?>> getTypeClasses() {
        return List.of(MenuName.class);
    }

    @Override
    public List<String> getTypeStrings() {
        return List.of("menu-name");
    }
}

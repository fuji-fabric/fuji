package io.github.sakurawald.core.command.argument.adapter.impl;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import io.github.sakurawald.core.command.argument.structure.Argument;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.server.command.ServerCommandSource;

import java.util.List;

public class BossBarColorArgumentTypeAdapter extends BaseArgumentTypeAdapter {

    @Override
    protected ArgumentType<?> makeArgumentType() {
        return StringArgumentType.string();
    }

    @Override
    public RequiredArgumentBuilder<ServerCommandSource, ?> makeRequiredArgumentBuilder(String argumentName) {
        return super.makeRequiredArgumentBuilder(argumentName)
            .suggests(CommandHelper.Suggestion.enums(BossBar.Color::values));
    }

    @Override
    protected Object makeArgumentObject(CommandContext<ServerCommandSource> context, Argument argument) {
        String name = StringArgumentType.getString(context, argument.getArgumentName());
        return BossBar.Color.valueOf(name);
    }

    @Override
    public List<Class<?>> getTypeClasses() {
        return List.of(BossBar.Color.class);
    }

    @Override
    public List<String> getTypeStrings() {
        return List.of("bossbar-color");
    }
}

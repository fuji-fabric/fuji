package io.github.sakurawald.fuji.module.initializer.economy.command.argument.adapter;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import io.github.sakurawald.fuji.core.command.argument.structure.Argument;
import io.github.sakurawald.fuji.module.initializer.economy.command.argument.wrapper.CurrencyId;
import io.github.sakurawald.fuji.module.initializer.economy.service.EconomyService;
import java.util.List;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;

public class CurrencyIdArgumentTypeAdapter extends BaseArgumentTypeAdapter {
    @Override
    protected ArgumentType<?> makeArgumentType() {
        return IdentifierArgumentType.identifier();
    }

    @Override
    protected Object makeArgumentObject(CommandContext<ServerCommandSource> context, Argument argument) {
        Identifier identifier = IdentifierArgumentType.getIdentifier(context, argument.getArgumentName());
        return new CurrencyId(identifier.toString());
    }

    @Override
    public List<Class<?>> getTypeClasses() {
        return List.of(CurrencyId.class);
    }

    @Override
    public List<String> getTypeStrings() {
        return List.of("currency_id");
    }

    @Override
    public RequiredArgumentBuilder<ServerCommandSource, ?> makeRequiredArgumentBuilder(String argumentName) {
        return super.makeRequiredArgumentBuilder(argumentName)
            .suggests(CommandHelper.Suggestion.iterable(EconomyService::getServerCurrencyIds));
    }
}

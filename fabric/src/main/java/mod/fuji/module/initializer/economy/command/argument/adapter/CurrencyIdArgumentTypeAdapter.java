package mod.fuji.module.initializer.economy.command.argument.adapter;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import mod.fuji.core.command.argument.representation.IdentifierArgumentTypeIR;
import mod.fuji.core.command.argument.structure.CommandArgument;
import mod.fuji.core.structure.IdentifierIR;
import mod.fuji.module.initializer.economy.command.argument.wrapper.CurrencyId;
import mod.fuji.module.initializer.economy.service.EconomyService;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.NotNull;

public class CurrencyIdArgumentTypeAdapter extends BaseArgumentTypeAdapter {
    @Override
    protected ArgumentType<?> makeArgumentType() {
        return IdentifierArgumentTypeIR.makeArgumentType();
    }

    @Override
    protected Object makeArgumentValue(@NotNull CommandContext<CommandSourceStack> context, @NotNull CommandArgument commandArgument) {
        var nativeValue = IdentifierArgumentTypeIR.makeArgumentValue(context, commandArgument);
        return new CurrencyId(IdentifierIR.of(nativeValue));
    }

    @Override
    public List<Class<?>> getTypeClasses() {
        return List.of(CurrencyId.class);
    }

    @Override
    public List<String> getTypeNames() {
        return List.of("currency_id");
    }

    @Override
    @NotNull
    protected RequiredArgumentBuilder<CommandSourceStack, ?> makeRequiredArgumentBuilder(@NotNull String argumentName) {
        return super.makeRequiredArgumentBuilder(argumentName)
            .suggests(CommandHelper.Suggestion.iterable(EconomyService::getServerCurrencyIds));
    }
}

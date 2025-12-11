package mod.fuji.core.command.argument.adapter.impl;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import java.util.List;
import mod.fuji.Fuji;
import mod.fuji.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import mod.fuji.core.command.argument.structure.CommandArgument;
import mod.fuji.core.command.argument.wrapper.impl.FujiIdentifier;
import mod.fuji.core.structure.IdentifierIR;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.NotNull;

public class FujiIdentifierArgumentTypeAdapter extends BaseArgumentTypeAdapter {

    @Override
    public List<Class<?>> getTypeClasses() {
        return List.of(FujiIdentifier.class);
    }

    @Override
    public List<String> getTypeNames() {
        return List.of("fuji-identifier");
    }

    @Override
    protected ArgumentType<?> makeArgumentType() {
        return StringArgumentType.string();
    }

    @Override
    protected Object makeArgumentValue(@NotNull CommandContext<CommandSourceStack> context, @NotNull CommandArgument commandArgument) {
        String idString = StringArgumentType.getString(context, commandArgument.getArgumentName());

        IdentifierIR id;
        if(idString.contains(":")) {
            id = IdentifierIR.makeIdentifierOrThrow(idString);
        } else {
            id = IdentifierIR.makeIdentifierOrThrow(Fuji.MOD_ID + ":" + idString);
        }

        return new FujiIdentifier(id);
    }
}

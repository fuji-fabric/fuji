package mod.fuji.core.command.argument.adapter.impl;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import java.util.List;
import mod.fuji.core.auxiliary.minecraft.RegistryHelper;
import mod.fuji.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import mod.fuji.core.command.argument.structure.CommandArgument;
import mod.fuji.core.command.argument.wrapper.impl.FujiIdentifier;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;
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
    protected Object makeArgumentValue(@NotNull CommandContext<ServerCommandSource> context, @NotNull CommandArgument commandArgument) {
        String idString = StringArgumentType.getString(context, commandArgument.getArgumentName());

        Identifier id;
        if(idString.contains(":")) {
            id = RegistryHelper.makeIdentifierOrThrow(idString);
        } else {
            id = RegistryHelper.makeIdentifierOrThrow("fuji:" + idString);
        }

        return new FujiIdentifier(id);
    }
}

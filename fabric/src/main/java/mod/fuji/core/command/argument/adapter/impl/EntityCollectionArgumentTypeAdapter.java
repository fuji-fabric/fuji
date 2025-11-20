package mod.fuji.core.command.argument.adapter.impl;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mod.fuji.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import mod.fuji.core.command.argument.structure.CommandArgument;
import mod.fuji.core.command.argument.wrapper.impl.EntityCollection;
import java.util.List;
import lombok.SneakyThrows;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.NotNull;

public class EntityCollectionArgumentTypeAdapter extends BaseArgumentTypeAdapter {
    @Override
    public List<Class<?>> getTypeClasses() {
        return List.of(EntityCollection.class);
    }

    @Override
    public List<String> getTypeNames() {
        return List.of("entities");
    }

    @Override
    protected ArgumentType<?> makeArgumentType() {
        return EntityArgument.entities();
    }

    @SneakyThrows(CommandSyntaxException.class)
    @Override
    protected Object makeArgumentValue(@NotNull CommandContext<CommandSourceStack> context, @NotNull CommandArgument commandArgument) {
        return new EntityCollection(EntityArgument.getEntities(context, commandArgument.getArgumentName()));
    }
}

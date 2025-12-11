package mod.fuji.core.command.argument.representation;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import mod.fuji.core.command.argument.structure.CommandArgument;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.NotNull;

public class IdentifierArgumentTypeIR {

    public static ArgumentType<?> makeArgumentType() {
        #if MC_VER < MC_1_21_11
        return net.minecraft.commands.arguments.ResourceLocationArgument.id();
        #elif MC_VER >= MC_1_21_11
        return net.minecraft.commands.arguments.IdentifierArgument.id();
        #endif
    }

    public static
    #if MC_VER < MC_1_21_11
    net.minecraft.resources.ResourceLocation
    #elif MC_VER >= MC_1_21_11
    net.minecraft.resources.Identifier
    #endif
    makeArgumentValue(@NotNull CommandContext<CommandSourceStack> context, @NotNull CommandArgument commandArgument) {
        #if MC_VER < MC_1_21_11
        return net.minecraft.commands.arguments.ResourceLocationArgument.getId(context, commandArgument.getArgumentName());
        #elif MC_VER >= MC_1_21_11
        return net.minecraft.commands.arguments.IdentifierArgument.getId(context, commandArgument.getArgumentName());
        #endif
    }

}

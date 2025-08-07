package io.github.sakurawald.fuji.module.mixin.command_permission;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.module.initializer.command_permission.CommandPermissionInitializer;
import io.github.sakurawald.fuji.module.initializer.command_permission.structure.WrappedPredicate;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Predicate;

@Mixin(value = CommandNode.class, remap = false)
public class CommandNodeMixin {

    @SuppressWarnings("unchecked")
    @Unique
    final CommandNode<ServerCommandSource> node = (CommandNode<ServerCommandSource>) (Object) this;
    @Mutable
    @Shadow
    @Final
    private Predicate<ServerCommandSource> requirement;

    @SuppressWarnings("unchecked")
    @ModifyReturnValue(method = "getRequirement", at = @At("RETURN"))
    private Predicate<?> wrapRequirementPredicateForThisCommandNode(Predicate<?> original) {

        /* Only try to wrap the requirement predicate of command node until the command dispatcher is initialized. */
        @Nullable CommandDispatcher<ServerCommandSource> dispatcher = CommandHelper.getCommandDispatcher();
        if (dispatcher == null) {
            LogUtil.debug("The CommandNode#getRequirement is triggered too early, fuji will just ignore this call.");
            return original;
        }

        /* Wrap the requirement predicate for command node. */
        if (!(original instanceof WrappedPredicate<?>)) {
            String path = CommandHelper.Node.findCommandNodePath(node);
            requirement = CommandPermissionInitializer.makeWrappedPredicate(path, (Predicate<ServerCommandSource>) original);
            return requirement;
        }

        return original;
    }

}

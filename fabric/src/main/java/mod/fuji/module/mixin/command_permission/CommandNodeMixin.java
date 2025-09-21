package mod.fuji.module.mixin.command_permission;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import mod.fuji.core.auxiliary.LogUtil;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.module.initializer.command_permission.service.CommandPermissionService;
import mod.fuji.module.initializer.command_permission.structure.WrappedPredicate;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Predicate;

@Mixin(value = CommandNode.class, remap = false)
public class CommandNodeMixin {

    @Mutable
    @Shadow
    @Final
    private Predicate<Object> requirement;

    @SuppressWarnings({"unchecked", "ConstantValue"})
    @ModifyReturnValue(method = "getRequirement", at = @At("RETURN"))
    private Predicate<Object> wrapRequirementPredicateForThisCommandNode(Predicate<Object> original) {

        /* Only try to wrap the requirement predicate of command node until the command dispatcher is initialized. */
        @Nullable CommandDispatcher<?> dispatcher = CommandHelper.getCommandDispatcher();
        if (dispatcher == null) {
            LogUtil.debug("The CommandNode#getRequirement is triggered too early, fuji will just ignore this call.");
            return original;
        }

        /* Wrap the requirement predicate for command node. */
        if (!(original instanceof WrappedPredicate<?>)) {
            final CommandNode<ServerCommandSource> node = (CommandNode<ServerCommandSource>) (Object) this;
            requirement = CommandPermissionService.makeWrappedPredicate(node, original);
            return requirement;
        }

        /* The requirement is already wrapped, simply return it. */
        return original;
    }

}

package mod.fuji.module.mixin.command_permission;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import mod.fuji.core.auxiliary.LogUtil;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.module.initializer.command_permission.service.CommandPermissionService;
import mod.fuji.module.initializer.command_permission.structure.WrappedPredicate;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Predicate;

@Mixin(value = CommandNode.class, remap = false)
public abstract class CommandNodeMixin {

    @Mutable
    @Shadow
    @Final
    private Predicate<Object> requirement;

    @Unique
    private Predicate<Object> previousWrappedPredicate;

    @SuppressWarnings({"unchecked", "ConstantValue"})
    @ModifyReturnValue(method = "getRequirement", at = @At("RETURN"))
    Predicate<Object> wrapRequirementPredicateForThisCommandNode(Predicate<Object> original) {

        /* Only try to wrap the requirement predicate of command node until the command dispatcher is initialized. */
        @Nullable CommandDispatcher<?> dispatcher = CommandHelper.getCommandDispatcher();
        if (dispatcher == null) {
            LogUtil.debug("The CommandNode#getRequirement is triggered too early, fuji will just ignore this call.");
            return original;
        }

        /* Wrap the requirement predicate for command node. */
        boolean shouldWrapIt = false;
        if (!(original instanceof WrappedPredicate<?>)) {
            shouldWrapIt = true;
        } else if (!this.requirement.equals(this.previousWrappedPredicate)) {
            // NOTE: Re-wrap if someone else sets a new value to this.requirement variable.
            shouldWrapIt = true;
        }

        if (shouldWrapIt) {
            // NOTE: Wrap the currently captured this.requirement value.
            final CommandNode<CommandSourceStack> node = (CommandNode<CommandSourceStack>) (Object) this;
            requirement = CommandPermissionService.makeWrappedPredicate(node, original);

            // NOTE: Store previous wrapped predicate, and invalidate it if someone modifies the this.requirement value.
            previousWrappedPredicate = requirement;
            return requirement;
        }

        /* The requirement is already wrapped, simply return it. */
        return original;
    }

}

package mod.fuji.module.mixin.core.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.Map;
import mod.fuji.core.command.extension.CommandNodeExtension;
import java.util.function.Predicate;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = CommandNode.class, remap = false)
public class CommandNodeMixin<S> implements CommandNodeExtension<S> {

    // TODO(Ravel): Could not determine a single target
    @Mutable
    @Shadow
    @Final
    private Predicate<S> requirement;

    // TODO(Ravel): Could not determine a single target
    @Shadow
    @Final
    private Map<String, CommandNode<S>> children;

    // TODO(Ravel): Could not determine a single target
    @Shadow
    @Final
    private Map<String, LiteralCommandNode<S>> literals;

    // TODO(Ravel): Could not determine a single target
    @Shadow
    @Final
    private Map<String, ArgumentCommandNode<S, ?>> arguments;

    // TODO(Ravel): Could not determine a single target
    @Shadow
    private Command<S> command;

    // TODO(Ravel): Could not determine a single target
    @Mutable
    @Shadow
    @Final
    private CommandNode<S> redirect;

    @Override
    public Predicate<S> fuji$getRequirement() {
        return this.requirement;
    }

    @Override
    public void fuji$setRequirement(@NotNull Predicate<S> requirement) {
        this.requirement = requirement;
    }

    @Override
    public void fuji$setCommand(Command<S> command) {
        this.command = command;
    }

    @Override
    public void fuji$setRedirect(CommandNode<S> command) {
        this.redirect = command;
    }

    @Override
    public Map<String, CommandNode<S>> fuji$getChildren() {
        return this.children;
    }

    @Override
    public Map<String, LiteralCommandNode<S>> fuji$getLiterals() {
        return this.literals;
    }

    @Override
    public Map<String, ArgumentCommandNode<S, ?>> fuji$getArguments() {
        return this.arguments;
    }

}


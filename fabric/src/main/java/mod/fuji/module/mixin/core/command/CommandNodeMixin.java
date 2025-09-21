package mod.fuji.module.mixin.core.command;

import com.mojang.brigadier.tree.CommandNode;
import mod.fuji.core.command.extension.CommandNodeExtension;
import java.util.function.Predicate;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = CommandNode.class)
public class CommandNodeMixin<S> implements CommandNodeExtension<S> {

    @Mutable
    @Shadow(remap = false)
    @Final
    private Predicate<S> requirement;

    @Override
    public Predicate<S> fuji$getRequirement() {
        return this.requirement;
    }

    @Override
    public void fuji$setRequirement(@NotNull Predicate<S> requirement) {
        this.requirement = requirement;
    }
}

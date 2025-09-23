package mod.fuji.core.command.extension;

import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.Map;
import java.util.function.Predicate;

public interface CommandNodeExtension<S> {

    @SuppressWarnings("unused")
    Predicate<S> fuji$getRequirement();

    void fuji$setRequirement(Predicate<S> requirement);

    Map<String, CommandNode<S>> fuji$getChildren();

    Map<String, LiteralCommandNode<S>> fuji$getLiterals();

    Map<String, ArgumentCommandNode<S, ?>> fuji$getArguments();

}

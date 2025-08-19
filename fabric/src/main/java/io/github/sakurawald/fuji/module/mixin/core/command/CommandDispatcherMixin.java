package io.github.sakurawald.fuji.module.mixin.core.command;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.CommandNode;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.fuji.core.command.extension.CommandContextBuilderExtension;
import java.util.concurrent.CompletableFuture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(CommandDispatcher.class)
public class CommandDispatcherMixin<S> {

    // Apply patch: https://github.com/Mojang/brigadier/pull/142
    @SuppressWarnings({"unchecked", "rawtypes"})
    @ModifyVariable(method = "parseNodes", at = @At(value = "STORE"), ordinal = 2, remap = false)
    CommandContextBuilder passChildContextAfterRedirect(CommandContextBuilder<S> childContext, @Local(ordinal = 1) CommandContextBuilder<S> parentContext) {
        ServerHelper.withServerCommandSource(parentContext.getSource(), () -> {
            CommandContextBuilderExtension<S> accessor = (CommandContextBuilderExtension<S>) childContext;
            accessor.fuji$withArguments(parentContext.getArguments());
        });

        return childContext;
    }

    // Apply patch: https://github.com/Mojang/brigadier/pull/157
    @WrapOperation(method = "getCompletionSuggestions(Lcom/mojang/brigadier/ParseResults;I)Ljava/util/concurrent/CompletableFuture;"
        , at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/tree/CommandNode;listSuggestions(Lcom/mojang/brigadier/context/CommandContext;Lcom/mojang/brigadier/suggestion/SuggestionsBuilder;)Ljava/util/concurrent/CompletableFuture;")
        , remap = false)
    CompletableFuture<Suggestions> f(CommandNode<S> instance, CommandContext<S> commandContext, SuggestionsBuilder suggestionsBuilder, Operation<CompletableFuture<Suggestions>> original, @Local(argsOnly = true) ParseResults<S> parse) {
        S commandSource = parse.getContext().getSource();

        if (instance.canUse(commandSource)) {
            return original.call(instance, commandContext, suggestionsBuilder);
        }

        return Suggestions.empty();
    }

}

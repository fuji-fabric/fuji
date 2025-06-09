package io.github.sakurawald.core.command.extension;

import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedArgument;

import java.util.Map;

public interface CommandContextBuilderExtension<S> {

    @SuppressWarnings("EmptyMethod")
    Map<String, ParsedArgument<S, ?>> getArguments();

    @SuppressWarnings("UnusedReturnValue")
    CommandContextBuilder<S> fuji$withArguments(Map<String, ParsedArgument<S, ?>> arguments);
}

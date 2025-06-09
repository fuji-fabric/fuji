package io.github.sakurawald.module.initializer.command_bundle.accessor;

import com.mojang.brigadier.context.ParsedArgument;

import java.util.Map;

public interface CommandContextAccessor<S> {

    Map<String, ParsedArgument<S, ?>> fuji$getArguments();

}

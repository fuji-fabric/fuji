package io.github.sakurawald.module.initializer.command_scheduler.command.argument.adapter;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import io.github.sakurawald.core.command.argument.structure.Argument;
import io.github.sakurawald.module.initializer.command_scheduler.CommandSchedulerInitializer;
import io.github.sakurawald.module.initializer.command_scheduler.command.argument.wrapper.JobName;
import io.github.sakurawald.module.initializer.command_scheduler.structure.Job;
import net.minecraft.server.command.ServerCommandSource;

import java.util.List;

public class JobNameArgumentTypeAdapter extends BaseArgumentTypeAdapter {

    @Override
    protected ArgumentType<?> makeArgumentType() {
        return StringArgumentType.string();
    }

    @Override
    public Object makeArgumentObject(CommandContext<ServerCommandSource> context, Argument argument) {
        return new JobName(StringArgumentType.getString(context, argument.getArgumentName()));
    }

    @Override
    public List<Class<?>> getTypeClasses() {
        return List.of(JobName.class);
    }

    @Override
    public List<String> getTypeStrings() {
        return List.of("schedule-job-name", "job-name");
    }

    @Override
    public RequiredArgumentBuilder<ServerCommandSource, ?> makeRequiredArgumentBuilder(String argumentName) {
        return super.makeRequiredArgumentBuilder(argumentName).suggests(CommandHelper.Suggestion.iterable(
            () -> CommandSchedulerInitializer.scheduler.model().jobs.stream().map(Job::getName).toList()
        ));
    }
}

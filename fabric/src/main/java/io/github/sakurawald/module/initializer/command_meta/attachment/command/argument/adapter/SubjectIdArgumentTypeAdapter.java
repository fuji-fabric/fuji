package io.github.sakurawald.module.initializer.command_meta.attachment.command.argument.adapter;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import io.github.sakurawald.core.command.argument.structure.Argument;
import io.github.sakurawald.core.manager.Managers;
import io.github.sakurawald.module.initializer.command_meta.attachment.command.argument.wrapper.SubjectId;
import net.minecraft.server.command.ServerCommandSource;

import java.util.List;

public class SubjectIdArgumentTypeAdapter extends BaseArgumentTypeAdapter {

    @Override
    protected ArgumentType<?> makeArgumentType() {
        return StringArgumentType.string();
    }

    @Override
    public Object makeArgumentObject(CommandContext<ServerCommandSource> context, Argument argument) {
        return new SubjectId(StringArgumentType.getString(context, argument.getArgumentName()));
    }

    @Override
    public List<Class<?>> getTypeClasses() {
        return List.of(SubjectId.class);
    }

    @Override
    public List<String> getTypeStrings() {
        return List.of("subject-id");
    }

    @Override
    public RequiredArgumentBuilder<ServerCommandSource, ?> makeRequiredArgumentBuilder(String argumentName) {
        return super.makeRequiredArgumentBuilder(argumentName).suggests((ctx, builder) -> {
            String subject = StringArgumentType.getString(ctx, "subject");
            Managers.getAttachmentManager().listSubjectId(subject).forEach(builder::suggest);

            return builder.buildFuture();
        });
    }
}

package io.github.sakurawald.fuji.module.initializer.command_attachment.service;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.exception.AbortCommandExecutionException;
import io.github.sakurawald.fuji.core.command.executor.CommandExecutor;
import io.github.sakurawald.fuji.core.command.structure.ExtendedCommandSource;
import io.github.sakurawald.fuji.module.initializer.command_attachment.CommandAttachmentInitializer;
import io.github.sakurawald.fuji.module.initializer.command_attachment.command.argument.wrapper.ExecuteAsType;
import io.github.sakurawald.fuji.module.initializer.command_attachment.command.argument.wrapper.InteractType;
import io.github.sakurawald.fuji.module.initializer.command_attachment.config.model.CommandAttachmentModel;
import io.github.sakurawald.fuji.module.initializer.command_attachment.structure.CommandAttachmentDataNode;
import io.github.sakurawald.fuji.module.initializer.command_attachment.structure.attachment_entry.BaseCommandAttachmentEntry;
import io.github.sakurawald.fuji.module.initializer.command_attachment.structure.attachment_entry.ItemStackCommandAttachmentEntry;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CommandAttachmentService {

    public static <T> T withAttachmentDataNode(@NotNull String uuid, @NotNull Function<CommandAttachmentDataNode, T> function) {
        Optional<CommandAttachmentDataNode> first = findAttachmentDataNode(uuid);

        CommandAttachmentDataNode dataNode = first.orElseGet(() -> {
            CommandAttachmentDataNode newValue = new CommandAttachmentDataNode();
            newValue.setId(uuid);
            getCommandAttachmentDataNodes().add(newValue);
            return newValue;
        });

        return function.apply(dataNode);
    }

    private static Optional<CommandAttachmentDataNode> findAttachmentDataNode(@Nullable String uuid) {
        return getCommandAttachmentDataNodes()
            .stream()
            .filter(it -> it.getId().equals(uuid))
            .findFirst();
    }

    private static List<CommandAttachmentDataNode> getCommandAttachmentDataNodes() {
        return CommandAttachmentInitializer.data.model()
            .getNodes();
    }

    public static void tryTriggerAttachmentModel(@Nullable String uuid, @NotNull PlayerEntity player, @NotNull List<InteractType> receivedInteractTypes, @NotNull Runnable postTriggered) {
        findAttachmentDataNode(uuid)
            .ifPresent(it -> triggerAttachmentModel(it.getModel(), uuid, player, receivedInteractTypes, postTriggered));
    }

    private static void triggerAttachmentModel(@NotNull CommandAttachmentModel model, String uuid, @NotNull PlayerEntity player, @NotNull List<InteractType> receivedInteractTypes, @NotNull Runnable postTriggered) {
        /* Process attachment nodes. */
        for (BaseCommandAttachmentEntry e : model.getEntries()) {
            /* Filter for interaction type. */
            if (!receivedInteractTypes.contains(e.getInteractType())) continue;

            /* Filter for usage times limit. */
            if (e.getUseTimes() >= e.getMaxUseTimes()) continue;

            /* Switch for execute-as-type. */
            ExecuteAsType executeAsType = e.getExecuteAsType();
            ServerCommandSource source = CommandHelper.Source.getCommandSource(player);
            switch (executeAsType) {
                case CONSOLE ->
                    CommandExecutor.execute(ExtendedCommandSource.asConsole(source), e.getCommand());
                case PLAYER ->
                    CommandExecutor.execute(ExtendedCommandSource.asPlayer(source, (ServerPlayerEntity) player), e.getCommand());
                case FAKE_OP ->
                    CommandExecutor.execute(ExtendedCommandSource.asFakeOp(source, (ServerPlayerEntity) player), e.getCommand());
            }

            /* Eval post-triggered function. */
            postTriggered.run();

            /* Handler for destroy-item. */
            e.setUseTimes(e.getUseTimes() + 1);
            if (e instanceof ItemStackCommandAttachmentEntry ie) {
                if (ie.isDestroyItem() && e.getUseTimes() >= e.getMaxUseTimes()) {
                    player.getMainHandStack().decrement(1);
                }
            }
        }
    }

    public static void removeAttachmentModel(@NotNull String uuid) {
        getCommandAttachmentDataNodes()
            .removeIf(it -> it.getId().equals(uuid));
    }

    public static int queryAttachmentModel(@NotNull ServerCommandSource source, @Nullable String uuid) {
        return findAttachmentDataNode(uuid)
            .map(it -> {
                String attachmentDataNodeString = it.toString();
                source.sendMessage(Text.literal(attachmentDataNodeString));
                return CommandHelper.Return.SUCCESS;
            })
            .orElseThrow(() -> {
                TextHelper.sendTextByKey(source, "command_attachment.query.no_attachment");
                return new AbortCommandExecutionException();
            });
    }
}

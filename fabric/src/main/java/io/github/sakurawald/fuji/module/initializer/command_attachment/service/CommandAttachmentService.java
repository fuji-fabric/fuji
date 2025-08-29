package io.github.sakurawald.fuji.module.initializer.command_attachment.service;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.exception.AbortCommandExecutionException;
import io.github.sakurawald.fuji.core.command.executor.CommandExecutor;
import io.github.sakurawald.fuji.core.command.executor.structure.ExtendedCommandSource;
import io.github.sakurawald.fuji.core.config.mapper.GsonMapper;
import io.github.sakurawald.fuji.module.initializer.command_attachment.CommandAttachmentInitializer;
import io.github.sakurawald.fuji.module.initializer.command_attachment.command.argument.wrapper.ExecuteAsType;
import io.github.sakurawald.fuji.module.initializer.command_attachment.command.argument.wrapper.InteractType;
import io.github.sakurawald.fuji.module.initializer.command_attachment.structure.CommandAttachmentDataNode;
import io.github.sakurawald.fuji.module.initializer.command_attachment.structure.CommandAttachments;
import io.github.sakurawald.fuji.module.initializer.command_attachment.structure.attachment_entry.BaseCommandAttachmentEntry;
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
        CommandAttachmentDataNode dataNode = findAttachmentDataNode(uuid)
            .orElseGet(() -> {
                CommandAttachmentDataNode newValue = new CommandAttachmentDataNode();
                newValue.setId(uuid);
                listAttachmentDataNodes().add(newValue);
                return newValue;
            });

        return function.apply(dataNode);
    }

    public static Optional<CommandAttachmentDataNode> findAttachmentDataNode(@Nullable String uuid) {
        return listAttachmentDataNodes()
            .stream()
            .filter(it -> it.getId().equals(uuid))
            .findFirst();
    }

    private static List<CommandAttachmentDataNode> listAttachmentDataNodes() {
        return CommandAttachmentInitializer.data.model()
            .getNodes();
    }

    public static void tryTriggerAttachmentDataNode(@Nullable String uuid, @NotNull PlayerEntity player, @NotNull List<InteractType> inputInteractTypes, @NotNull Runnable postTriggered) {
        findAttachmentDataNode(uuid)
            .ifPresent(it -> tryTriggerCommandAttachments(it.getAttachments(), player, inputInteractTypes, postTriggered));
    }

    private static void tryTriggerCommandAttachments(@NotNull CommandAttachments attachments, @NotNull PlayerEntity player, @NotNull List<InteractType> inputInteractTypes, @NotNull Runnable triggeredHook) {
        ServerHelper.withServerPlayerEntity(player, () -> {
            /* Process attachment nodes. */
            for (BaseCommandAttachmentEntry entry : attachments.getEntries()) {
                /* Filtered by interaction type. */
                if (!inputInteractTypes.contains(entry.getInteractType())) continue;

                /* Filtered by usage times limit. */
                if (entry.getUseTimes() >= entry.getMaxUseTimes()) continue;

                /* Consume it first. */
                entry.onUsed((ServerPlayerEntity) player);

                /* Switch by execute-as-type. */
                ExecuteAsType executeAsType = entry.getExecuteAsType();
                ServerCommandSource initialingCommandSource = CommandHelper.Source.getCommandSource(player);
                switch (executeAsType) {
                    case CONSOLE ->
                        CommandExecutor.executeSingle(ExtendedCommandSource.asConsole(initialingCommandSource), entry.getCommand());
                    case PLAYER ->
                        CommandExecutor.executeSingle(ExtendedCommandSource.asPlayer(initialingCommandSource, (ServerPlayerEntity) player), entry.getCommand());
                    case FAKE_OP ->
                        CommandExecutor.executeSingle(ExtendedCommandSource.asFakeOp(initialingCommandSource, (ServerPlayerEntity) player), entry.getCommand());
                }

                /* Call hooks. */
                triggeredHook.run();
            }
        });
    }

    public static void removeAttachmentDataNode(@NotNull String uuid) {
        listAttachmentDataNodes()
            .removeIf(it -> it.getId().equals(uuid));
    }

    public static int printAttachmentDataNode(@NotNull ServerCommandSource source, Optional<String> uuid) {
        return uuid
            .flatMap(CommandAttachmentService::findAttachmentDataNode)
            .map(it -> {
                @SuppressWarnings("UnnecessaryLocalVariable")
                CommandAttachmentDataNode dataNode = it;
                String attachmentDataNode = GsonMapper.toJsonString(dataNode);
                TextHelper.sendMessageByText(source, Text.literal(attachmentDataNode));
                return CommandHelper.Return.SUCCESS;
            })
            .orElseThrow(() -> {
                TextHelper.sendTextByKey(source, "command_attachment.query.no_attachment");
                return new AbortCommandExecutionException();
            });
    }
}

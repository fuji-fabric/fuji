package mod.fuji.module.initializer.command_attachment.service;

import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.command.exception.AbortCommandExecutionException;
import mod.fuji.core.command.executor.CommandExecutor;
import mod.fuji.core.command.executor.structure.ExtendedCommandSource;
import mod.fuji.core.config.mapper.GsonMapper;
import mod.fuji.module.initializer.command_attachment.CommandAttachmentInitializer;
import mod.fuji.module.initializer.command_attachment.command.argument.wrapper.ExecuteAsType;
import mod.fuji.module.initializer.command_attachment.command.argument.wrapper.InteractType;
import mod.fuji.module.initializer.command_attachment.structure.CommandAttachmentDataNode;
import mod.fuji.module.initializer.command_attachment.structure.CommandAttachments;
import mod.fuji.module.initializer.command_attachment.structure.attachment_entry.BaseCommandAttachmentEntry;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.world.entity.player.Player;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
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

    public static void tryTriggerAttachmentDataNode(@Nullable String uuid, @NotNull Player player, @NotNull List<InteractType> inputInteractTypes, @NotNull Runnable postTriggered) {
        findAttachmentDataNode(uuid)
            .ifPresent(it -> tryTriggerCommandAttachments(it.getAttachments(), player, inputInteractTypes, postTriggered));
    }

    private static void tryTriggerCommandAttachments(@NotNull CommandAttachments attachments, @NotNull Player player, @NotNull List<InteractType> inputInteractTypes, @NotNull Runnable triggeredHook) {
        PlayerHelper.Kind.ifServerPlayerEntity(player, (serverPlayer) -> {
            /* Process attachment nodes. */
            for (BaseCommandAttachmentEntry entry : attachments.getEntries()) {
                /* Filtered by interaction type. */
                if (!inputInteractTypes.contains(entry.getInteractType())) continue;

                /* Filtered by usage times limit. */
                if (entry.getUseTimes() >= entry.getMaxUseTimes()) continue;

                /* Consume it first. */
                entry.onUsed((ServerPlayer) player);

                /* Switch by execute-as-type. */
                ExecuteAsType executeAsType = entry.getExecuteAsType();
                CommandSourceStack initialingCommandSource = CommandHelper.Source.getCommandSource(serverPlayer);
                switch (executeAsType) {
                    case CONSOLE ->
                        CommandExecutor.executeSingle(ExtendedCommandSource.asConsole(initialingCommandSource), entry.getCommand());
                    case PLAYER ->
                        CommandExecutor.executeSingle(ExtendedCommandSource.asPlayer(initialingCommandSource, (ServerPlayer) player), entry.getCommand());
                    case FAKE_OP ->
                        CommandExecutor.executeSingle(ExtendedCommandSource.asFakeOp(initialingCommandSource, (ServerPlayer) player), entry.getCommand());
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

    public static int printAttachmentDataNode(@NotNull CommandSourceStack source, Optional<String> uuid) {
        return uuid
            .flatMap(CommandAttachmentService::findAttachmentDataNode)
            .map(it -> {
                @SuppressWarnings("UnnecessaryLocalVariable")
                CommandAttachmentDataNode dataNode = it;
                String attachmentDataNode = GsonMapper.toJsonString(dataNode);
                TextHelper.sendMessageByText(source, Component.literal(attachmentDataNode));
                return CommandHelper.Return.SUCCESS;
            })
            .orElseThrow(() -> {
                TextHelper.sendTextByKey(source, "command_attachment.query.no_attachment");
                return new AbortCommandExecutionException();
            });
    }
}

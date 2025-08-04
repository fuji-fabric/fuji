package io.github.sakurawald.fuji.module.initializer.command_attachment.service;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.exception.AbortCommandExecutionException;
import io.github.sakurawald.fuji.core.command.executor.CommandExecutor;
import io.github.sakurawald.fuji.core.command.structure.ExtendedCommandSource;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.manager.Managers;
import io.github.sakurawald.fuji.module.initializer.command_attachment.CommandAttachmentInitializer;
import io.github.sakurawald.fuji.module.initializer.command_attachment.command.argument.wrapper.ExecuteAsType;
import io.github.sakurawald.fuji.module.initializer.command_attachment.command.argument.wrapper.InteractType;
import io.github.sakurawald.fuji.module.initializer.command_attachment.config.model.CommandAttachmentModel;
import io.github.sakurawald.fuji.module.initializer.command_attachment.structure.CommandAttachmentDataNode;
import io.github.sakurawald.fuji.module.initializer.command_attachment.structure.CommandAttachmentNode;
import io.github.sakurawald.fuji.module.initializer.command_attachment.structure.ItemStackCommandAttachmentNode;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import lombok.SneakyThrows;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CommandAttachmentService {

    private static final String COMMAND_ATTACHMENT_SUBJECT_NAME = "command-attachment";

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

    public static CommandAttachmentModel getAttachmentModel(@NotNull String uuid) {
        CommandAttachmentModel model;
        try {
            String attachment = Managers.getAttachmentManager().getAttachment(COMMAND_ATTACHMENT_SUBJECT_NAME, uuid);
            model = BaseConfigurationHandler.getGson().fromJson(attachment, CommandAttachmentModel.class);
        } catch (IOException e) {
            model = new CommandAttachmentModel();
            setAttachmentModel(uuid, model);
        }

        return model;
    }

    @SneakyThrows(IOException.class)
    public static void setAttachmentModel(String uuid, CommandAttachmentModel model) {
        String json = BaseConfigurationHandler.getGson().toJson(model);
        Managers.getAttachmentManager().setAttachment(COMMAND_ATTACHMENT_SUBJECT_NAME, uuid, json);
    }

    public static void tryTriggerAttachmentModel(@Nullable String uuid, @NotNull PlayerEntity player, @NotNull List<InteractType> receivedInteractTypes) {
        tryTriggerAttachmentModel(uuid,player,receivedInteractTypes, () -> {});
    }

    public static void tryTriggerAttachmentModel(@Nullable String uuid, @NotNull PlayerEntity player, @NotNull List<InteractType> receivedInteractTypes, @NotNull Runnable postTriggered) {
        findAttachmentDataNode(uuid)
            .ifPresent(it -> triggerAttachmentModel(it.getEntries(), uuid, player, receivedInteractTypes, postTriggered));
    }

    private static void triggerAttachmentModel(@NotNull CommandAttachmentModel model, String uuid, @NotNull PlayerEntity player, @NotNull List<InteractType> receivedInteractTypes, @NotNull Runnable postTriggered) {
        /* Process attachment nodes. */
        for (CommandAttachmentNode e : model.getEntries()) {
            /* Filter for interaction type. */
            if (!receivedInteractTypes.contains(e.getInteractType())) continue;

            /* Filter for usage times limit. */
            if (e.getUseTimes() >= e.getMaxUseTimes()) continue;

            /* Switch for execute-as-type. */
            ExecuteAsType executeAsType = e.getExecuteAsType();
            ServerCommandSource source = CommandHelper.Source.getCommandSource(player);
            switch (executeAsType) {
                case CONSOLE -> CommandExecutor.execute(ExtendedCommandSource.asConsole(source), e.getCommand());
                case PLAYER ->
                    CommandExecutor.execute(ExtendedCommandSource.asPlayer(source, (ServerPlayerEntity) player), e.getCommand());
                case FAKE_OP ->
                    CommandExecutor.execute(ExtendedCommandSource.asFakeOp(source, (ServerPlayerEntity) player), e.getCommand());
            }

            /* Eval post-triggered function. */
            postTriggered.run();

            /* Handler for destroy-item. */
            e.setUseTimes(e.getUseTimes() + 1);
            if (e instanceof ItemStackCommandAttachmentNode ie) {
                if (ie.isDestroyItem() && e.getUseTimes() >= e.getMaxUseTimes()) {
                    player.getMainHandStack().decrement(1);
                }
            }
        }

        /* Save the attachment model immediately. */
        setAttachmentModel(uuid, model);
    }

    public static void detachAttachment(@NotNull String uuid) {
        getCommandAttachmentDataNodes()
            .removeIf(it -> it.getId().equals(uuid));
    }

    public static int queryAttachment(@NotNull ServerCommandSource source, @Nullable String uuid) {
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

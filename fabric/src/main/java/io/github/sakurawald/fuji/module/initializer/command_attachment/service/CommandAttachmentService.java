package io.github.sakurawald.fuji.module.initializer.command_attachment.service;

import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.exception.AbortCommandExecutionException;
import io.github.sakurawald.fuji.core.command.executor.CommandExecutor;
import io.github.sakurawald.fuji.core.command.structure.ExtendedCommandSource;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.manager.Managers;
import io.github.sakurawald.fuji.module.initializer.command_attachment.command.argument.wrapper.ExecuteAsType;
import io.github.sakurawald.fuji.module.initializer.command_attachment.command.argument.wrapper.InteractType;
import io.github.sakurawald.fuji.module.initializer.command_attachment.config.model.CommandAttachmentModel;
import io.github.sakurawald.fuji.module.initializer.command_attachment.structure.CommandAttachmentNode;
import io.github.sakurawald.fuji.module.initializer.command_attachment.structure.ItemStackCommandAttachmentNode;
import java.io.IOException;
import java.util.List;
import lombok.SneakyThrows;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CommandAttachmentService {

    private static final String COMMAND_ATTACHMENT_SUBJECT_NAME = "command-attachment";

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean existsAttachmentModel(@Nullable String uuid) {
        return Managers.getAttachmentManager().existsAttachment(COMMAND_ATTACHMENT_SUBJECT_NAME, uuid);
    }

    @SneakyThrows(IOException.class)
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

    public static void tryTriggerAttachmentModel(@Nullable String uuid, PlayerEntity player, List<InteractType> receivedInteractTypes) {
        tryTriggerAttachmentModel(uuid,player,receivedInteractTypes, ()->{});
    }

    public static void tryTriggerAttachmentModel(@Nullable String uuid, PlayerEntity player, List<InteractType> receivedInteractTypes, Runnable postTriggered) {
        if (!existsAttachmentModel(uuid)) return;
        triggerAttachmentModel(uuid, player, receivedInteractTypes, postTriggered);
    }

    private static void triggerAttachmentModel(String uuid, PlayerEntity player, List<InteractType> receivedInteractTypes, Runnable postTriggered) {
        /* Get attachment model. */
        CommandAttachmentModel model = getAttachmentModel(uuid);

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

    @SneakyThrows
    public static void detachAttachment(@NotNull String uuid) {
        Managers.getAttachmentManager().unsetAttachment(COMMAND_ATTACHMENT_SUBJECT_NAME, uuid);
    }

    @SneakyThrows(IOException.class)
    public static void queryAttachment(ServerPlayerEntity player, String uuid) {
        /* Check if attachment exists. */
        if (!Managers.getAttachmentManager().existsAttachment(COMMAND_ATTACHMENT_SUBJECT_NAME, uuid)) {
            TextHelper.sendTextByKey(player, "command_attachment.query.no_attachment");
            throw new AbortCommandExecutionException();
        }

        /* Display it. */
        String attachment = Managers.getAttachmentManager().getAttachment(COMMAND_ATTACHMENT_SUBJECT_NAME, uuid);
        player.sendMessage(Text.literal(attachment));
        LogUtil.debug("Query the attachment: {}", attachment);
    }
}

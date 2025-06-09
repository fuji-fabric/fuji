package io.github.sakurawald.module.initializer.command_attachment;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import io.github.sakurawald.core.annotation.Document;
import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.auxiliary.minecraft.UuidHelper;
import io.github.sakurawald.core.command.annotation.CommandNode;
import io.github.sakurawald.core.command.annotation.CommandRequirement;
import io.github.sakurawald.core.command.annotation.CommandSource;
import io.github.sakurawald.core.command.argument.wrapper.impl.GreedyString;
import io.github.sakurawald.core.command.exception.AbortCommandExecutionException;
import io.github.sakurawald.core.command.executor.CommandExecutor;
import io.github.sakurawald.core.command.structure.ExtendedCommandSource;
import io.github.sakurawald.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.core.event.impl.ServerLifecycleEvents;
import io.github.sakurawald.core.manager.Managers;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import io.github.sakurawald.module.initializer.command_attachment.command.argument.wrapper.ExecuteAsType;
import io.github.sakurawald.module.initializer.command_attachment.command.argument.wrapper.InteractType;
import io.github.sakurawald.module.initializer.command_attachment.config.model.CommandAttachmentModel;
import io.github.sakurawald.module.initializer.command_attachment.job.TestSteppingOnBlockJob;
import io.github.sakurawald.module.initializer.command_attachment.structure.BlockCommandAttachmentNode;
import io.github.sakurawald.module.initializer.command_attachment.structure.CommandAttachmentNode;
import io.github.sakurawald.module.initializer.command_attachment.structure.CommandAttackmentType;
import io.github.sakurawald.module.initializer.command_attachment.structure.EntityCommandAttachmentNode;
import io.github.sakurawald.module.initializer.command_attachment.structure.ItemStackCommandAttachmentNode;
import lombok.SneakyThrows;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@CommandNode("command-attachment")
@CommandRequirement(level = 4)
public class CommandAttachmentInitializer extends ModuleInitializer {

    private static final String COMMAND_ATTACHMENT_SUBJECT_NAME = "command-attachment";

    private static final Map<String, String> player2uuid = new HashMap<>();

    private static void testSteppingBlockForPlayer(ServerPlayerEntity player) {
        String playerName = player.getGameProfile().getName();
        String originalUuid = player2uuid.get(playerName);
        String uuid = UuidHelper.getAttachedUuid(player.getServerWorld(), player.getSteppingPos());

        if (uuid.equals(originalUuid)) return;
        // update value
        player2uuid.put(playerName, uuid);

        // test stepping block
        if (!existsAttachmentModel(uuid)) return;
        ServerHelper.getServer().executeSync(() -> triggerAttachmentModel(uuid, player, List.of(InteractType.STEP_ON)));

    }

    public static void testSteppingBlockForPlayers() {
        ServerHelper.getPlayers().forEach(CommandAttachmentInitializer::testSteppingBlockForPlayer);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean existsAttachmentModel(String uuid) {
        return Managers.getAttachmentManager().existsAttachment(COMMAND_ATTACHMENT_SUBJECT_NAME, uuid);
    }

    @SneakyThrows(IOException.class)
    private static CommandAttachmentModel getAttachmentModel(String uuid) {

        CommandAttachmentModel model;
        try {
            String attachment = Managers.getAttachmentManager().getAttachment(COMMAND_ATTACHMENT_SUBJECT_NAME, uuid);
            model = BaseConfigurationHandler.getGson().fromJson(attachment, CommandAttachmentModel.class);
        } catch (IOException e) {
            model = new CommandAttachmentModel();
            String json = BaseConfigurationHandler.getGson().toJson(model);
            Managers.getAttachmentManager().setAttachment(COMMAND_ATTACHMENT_SUBJECT_NAME, uuid, json);
        }

        return model;
    }

    @SneakyThrows(IOException.class)
    private static void setAttachmentModel(String uuid, CommandAttachmentModel model) {
        String json = BaseConfigurationHandler.getGson().toJson(model);
        Managers.getAttachmentManager().setAttachment(COMMAND_ATTACHMENT_SUBJECT_NAME, uuid, json);
    }

    public static void triggerAttachmentModel(String uuid, PlayerEntity player, List<InteractType> receivedInteractTypes) {
        // get
        CommandAttachmentModel model = getAttachmentModel(uuid);

        // process
        for (CommandAttachmentNode e : model.getEntries()) {
            /* interaction type*/
            if (!receivedInteractTypes.contains(e.getInteractType())) continue;

            /* usage limit */
            if (e.getUseTimes() >= e.getMaxUseTimes()) continue;

            /* execute as */
            ExecuteAsType executeAsType = e.getExecuteAsType();
            ServerCommandSource source = player.getCommandSource((ServerWorld) player.getWorld());
            switch (executeAsType) {
                case CONSOLE -> CommandExecutor.execute(ExtendedCommandSource.asConsole(source), e.getCommand());
                case PLAYER ->
                    CommandExecutor.execute(ExtendedCommandSource.asPlayer(source, (ServerPlayerEntity) player), e.getCommand());
                case FAKE_OP ->
                    CommandExecutor.execute(ExtendedCommandSource.asFakeOp(source, (ServerPlayerEntity) player), e.getCommand());
            }

            /* item destroy */
            e.setUseTimes(e.getUseTimes() + 1);
            if (e instanceof ItemStackCommandAttachmentNode ie) {
                if (ie.isDestroyItem() && e.getUseTimes() >= e.getMaxUseTimes()) {
                    player.getMainHandStack().decrement(1);
                }
            }
        }

        // save
        setAttachmentModel(uuid, model);
    }

    @CommandNode("attach-item-one")
    @Document("Attach one command to an item.")
    private static int attachItemOne(@CommandSource ServerPlayerEntity player
        , @Document("The interaction type to trigger this command.") Optional<InteractType> interactType
        , @Document("Max use times of this command.") Optional<Integer> maxUseTimes
        , @Document("Execute this command as who?") Optional<ExecuteAsType> executeAsType
        , @Document("Should we destroy the item if the use times exceed.") Optional<Boolean> destroyItem
        , @Document("The command.") GreedyString command
    ) {
        // get model
        ItemStack mainHandStack = player.getMainHandStack();
        checkItemStackInHand(player, mainHandStack);

        String uuid = UuidHelper.getOrSetAttachedUuid(mainHandStack);
        CommandAttachmentModel model = getAttachmentModel(uuid);

        // new entry
        String $command = command.getValue();
        InteractType $interactType = interactType.orElse(InteractType.BOTH);
        ExecuteAsType $executeAsType = executeAsType.orElse(ExecuteAsType.FAKE_OP);
        Integer $maxUseTimes = maxUseTimes.orElse(Integer.MAX_VALUE);
        Boolean $destroyItem = destroyItem.orElse(true);

        model.getEntries().add(new ItemStackCommandAttachmentNode($command, $interactType, $executeAsType, $maxUseTimes, 0, $destroyItem));

        // save model
        setAttachmentModel(uuid, model);
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("attach-entity-one")
    @Document("Attach one command to an entity.")
    private static int attachEntityOne(@CommandSource ServerPlayerEntity player
        , @Document("The target entity.") Entity entity
        , @Document("The interaction type to trigger this command.") Optional<InteractType> interactType
        , @Document("Max use times of this command.") Optional<Integer> maxUseTimes
        , @Document("Execute this command as who?") Optional<ExecuteAsType> executeAsType
        , @Document("The command") GreedyString command
    ) {
        // get entity id
        String uuid = entity.getUuidAsString();
        CommandAttachmentModel model = getAttachmentModel(uuid);

        // new entry
        String $command = command.getValue();
        InteractType $interactType = interactType.orElse(InteractType.BOTH);
        ExecuteAsType $executeAsType = executeAsType.orElse(ExecuteAsType.FAKE_OP);
        Integer $maxUseTimes = maxUseTimes.orElse(Integer.MAX_VALUE);

        model.getEntries().add(new EntityCommandAttachmentNode($command, $interactType, $executeAsType, $maxUseTimes, 0));

        // save model
        setAttachmentModel(uuid, model);
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("attach-block-one")
    private static int attachBlockOne(@CommandSource ServerPlayerEntity player
        , BlockPos blockPos
        , @Document("The interaction type to trigger this command.") Optional<InteractType> interactType
        , @Document("Max use times of this command.") Optional<Integer> maxUseTimes
        , @Document("Execute this command as who?") Optional<ExecuteAsType> executeAsType
        , @Document("The command") GreedyString command
    ) {
        // get entity id
        String uuid = UuidHelper.getAttachedUuid(player.getServerWorld(), blockPos);
        CommandAttachmentModel model = getAttachmentModel(uuid);

        // new entry
        String $command = command.getValue();
        InteractType $interactType = interactType.orElse(InteractType.BOTH);
        ExecuteAsType $executeAsType = executeAsType.orElse(ExecuteAsType.FAKE_OP);
        Integer $maxUseTimes = maxUseTimes.orElse(Integer.MAX_VALUE);

        String createdIn = UuidHelper.toUuid(player.getWorld(), blockPos);
        model.getEntries().add(new BlockCommandAttachmentNode(createdIn, $command, $interactType, $executeAsType, $maxUseTimes, 0));

        // save model
        setAttachmentModel(uuid, model);
        return CommandHelper.Return.SUCCESS;
    }

    private static void checkItemStackInHand(ServerPlayerEntity player, ItemStack mainHandStack) {
        if (mainHandStack.isEmpty()) {
            TextHelper.sendMessageByKey(player, "item.empty.not_allow");
            throw new AbortCommandExecutionException();
        }
    }

    @CommandNode("detach-item-all")
    @Document("Detach all attached commands in the item.")
    private static int detachItemAll(@CommandSource ServerPlayerEntity player) {
        ItemStack mainHandStack = player.getMainHandStack();
        checkItemStackInHand(player, mainHandStack);
        String uuid = UuidHelper.getOrSetAttachedUuid(mainHandStack);

        doDetachAttachment(player, uuid);
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("detach-entity-all")
    @Document("Detach all attached commands in the entity.")
    private static int detachEntityAll(@CommandSource ServerPlayerEntity player, Entity entity) {
        String uuid = entity.getUuidAsString();

        doDetachAttachment(player, uuid);
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("detach-block-all")
    @Document("Detach all attached commands in the block.")
    private static int detachBlockAll(@CommandSource ServerPlayerEntity player, BlockPos blockPos) {
        String uuid = UuidHelper.getAttachedUuid(player.getServerWorld(), blockPos);

        doDetachAttachment(player, uuid);
        return CommandHelper.Return.SUCCESS;
    }

    @SneakyThrows
    private static void doDetachAttachment(ServerPlayerEntity player, String uuid) {
        Managers.getAttachmentManager().unsetAttachment(COMMAND_ATTACHMENT_SUBJECT_NAME, uuid);
    }

    @CommandNode("query-item")
    @Document("Query all attached commands in the item.")
    private static int queryItem(@CommandSource ServerPlayerEntity player) {
        ItemStack mainHandStack = player.getMainHandStack();
        checkItemStackInHand(player, mainHandStack);
        String uuid = UuidHelper.getAttachedUuid(mainHandStack.get(DataComponentTypes.CUSTOM_DATA));

        doQueryAttachment(player, uuid);
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("query-entity")
    @Document("Query all attached commands in the entity.")
    private static int queryEntity(@CommandSource ServerPlayerEntity player, Entity entity) {
        String uuid = entity.getUuidAsString();
        doQueryAttachment(player, uuid);
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("query-block")
    @Document("Query all attached commands in the block.")
    private static int queryBlock(@CommandSource ServerPlayerEntity player, BlockPos blockPos) {
        String uuid = UuidHelper.getAttachedUuid(player.getServerWorld(), blockPos);
        doQueryAttachment(player, uuid);
        return CommandHelper.Return.SUCCESS;
    }

    @SneakyThrows(IOException.class)
    private static void doQueryAttachment(ServerPlayerEntity player, String uuid) {
        if (!Managers.getAttachmentManager().existsAttachment(COMMAND_ATTACHMENT_SUBJECT_NAME, uuid)) {
            TextHelper.sendMessageByKey(player, "command_attachment.query.no_attachment");
            throw new AbortCommandExecutionException();
        }

        String attachment = Managers.getAttachmentManager().getAttachment(COMMAND_ATTACHMENT_SUBJECT_NAME, uuid);
        player.sendMessage(Text.literal(attachment));
    }

    @Override
    protected void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> new TestSteppingOnBlockJob().schedule());
    }

    @Override
    protected void registerGsonTypeAdapter() {
        BaseConfigurationHandler.registerTypeAdapter(CommandAttachmentNode.class, new CommandAttachmentNodeAdapter());
    }

    private static class CommandAttachmentNodeAdapter implements JsonDeserializer<CommandAttachmentNode> {

        @Override
        public @Nullable CommandAttachmentNode deserialize(@NotNull JsonElement json, Type typeOfT, @NotNull JsonDeserializationContext context) throws JsonParseException {
            if (!json.getAsJsonObject().has("type")) {
                // treat as item stack command attachment entry if type is null.
                json.getAsJsonObject().addProperty("type", CommandAttackmentType.ITEMSTACK.name());
            }


            String type = json.getAsJsonObject().get("type").getAsString();
            if (type.equals(CommandAttackmentType.ITEMSTACK.name()))
                return context.deserialize(json, ItemStackCommandAttachmentNode.class);
            if (type.equals(CommandAttackmentType.ENTITY.name()))
                return context.deserialize(json, EntityCommandAttachmentNode.class);
            if (type.equals(CommandAttackmentType.BLOCK.name()))
                return context.deserialize(json, BlockCommandAttachmentNode.class);

            throw new IllegalArgumentException("The type of command attachment entry is not supported");
        }

    }
}

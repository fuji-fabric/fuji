package io.github.sakurawald.fuji.module.initializer.command_attachment;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.ItemStackHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.EntityHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.UuidHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.GreedyString;
import io.github.sakurawald.fuji.core.command.exception.AbortCommandExecutionException;
import io.github.sakurawald.fuji.core.command.executor.CommandExecutor;
import io.github.sakurawald.fuji.core.command.structure.ExtendedCommandSource;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.event.impl.ServerLifecycleEvents;
import io.github.sakurawald.fuji.core.manager.Managers;
import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.command_attachment.command.argument.wrapper.ExecuteAsType;
import io.github.sakurawald.fuji.module.initializer.command_attachment.command.argument.wrapper.InteractType;
import io.github.sakurawald.fuji.module.initializer.command_attachment.config.model.CommandAttachmentModel;
import io.github.sakurawald.fuji.module.initializer.command_attachment.job.TestSteppingOnBlockJob;
import io.github.sakurawald.fuji.module.initializer.command_attachment.structure.BlockCommandAttachmentNode;
import io.github.sakurawald.fuji.module.initializer.command_attachment.structure.CommandAttachmentNode;
import io.github.sakurawald.fuji.module.initializer.command_attachment.structure.CommandAttackmentType;
import io.github.sakurawald.fuji.module.initializer.command_attachment.structure.EntityCommandAttachmentNode;
import io.github.sakurawald.fuji.module.initializer.command_attachment.structure.ItemStackCommandAttachmentNode;
import lombok.SneakyThrows;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
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

@Document(id = 1751826430284L, value = """
    This module allows you to attach commands into things:
    1. Attach commands into an item stack.
    2. Attach commands into a block.
    3. Attach commands into an entity.
    """)
@ColorBox(id = 1751870462624L, color = ColorBox.ColorBlockTypes.NOTE, value = """
    ◉ How it works?

    When you attach commands into an item.
    We will save a `binding ID` in the item NBT.
    Every item that has the same `binding ID` in its NBT data, shares the same `binding commands instance`.
    """)
@ColorBox(id = 1751870464919L, color = ColorBox.ColorBlockTypes.TIPS, value = """
    To give the item with attached commands.
    You can use `kit` module.
    The kit module will save the item NBT.
    So if you define a kit, puts the magic item in it.
    Then give the kit to a player.
    The player will get the magic item.
    """)
@ColorBox(id = 1751900879800L, color = ColorBox.ColorBlockTypes.EXAMPLE, value = """
    ◉ Make a magic-stick, which heals the player on clicked.
    Issue `/command-attachment attach-item-one heal`
    """)
@ColorBox(id = 1751900919703L, color = ColorBox.ColorBlockTypes.EXAMPLE, value = """
    ◉ Make a magic-stick, which:
    1. Gives diamonds * 1 on left clicked. (With use limit 3)
    2. Gives gold_ingot *1 on right clicked. (With use limit 5)

    Issue the following commands:
    1. `/command-attachment attach-item-one --maxUseTimes 3 --interactType LEFT give %player:name% minecraft:diamond 1`
    2. `/command-attachment attach-item-one --maxUseTimes 5 --interactType RIGHT give %player:name% minecraft:gold_ingot 1`
    """)
@ColorBox(id = 1751901028033L, color = ColorBox.ColorBlockTypes.EXAMPLE, value = """
    ◉ Make a magic-stick, which gives apple * 1, with use limit 3 times, and keep the item without destroying it.
    Issue: `/command-attachment attach-item-one --maxUseTimes 3 --destroyItem false give %player:name% minecraft:apple 1`.
    """)
@ColorBox(id = 1751901112988L, color = ColorBox.ColorBlockTypes.EXAMPLE, value = """
    ◉ Query the attached commands in a specific item.
    Issue `/command-attachment query-item`.
    """)
@ColorBox(id = 1751901147901L, color = ColorBox.ColorBlockTypes.EXAMPLE, value = """
    ◉ Let an entity say hello on right clicked.
    Issue: `/command-attachment attach-entity-one \\<entity-id\\> say hello %player:name%`
    """)
@ColorBox(id = 1751901221023L, color = ColorBox.ColorBlockTypes.EXAMPLE, value = """
    ◉ Make a portal block.
    Issue: `/command-attachment attach-block-one 0 0 0 --interactType STEP_ON tppos --targetPlayer %player:name% --dimension minecraft:the_end --x 0 --y 66 --z 0 %player:name%`.
    """)


@CommandNode("command-attachment")
@CommandRequirement(level = 4)
public class CommandAttachmentInitializer extends ModuleInitializer {

    private static final String COMMAND_ATTACHMENT_SUBJECT_NAME = "command-attachment";

    private static final Map<String, String> player2lastSteppingBlockUUID = new HashMap<>();

    private static void testSteppingBlockForPlayer(ServerPlayerEntity player) {
        String playerName = player.getGameProfile().getName();
        String originalUuid = player2lastSteppingBlockUUID.get(playerName);
        String uuid = UuidHelper.getAttachedUuid(EntityHelper.getServerWorld(player), player.getSteppingPos());

        /* Ignore the trigger if last stepping block is the same. */
        if (uuid.equals(originalUuid)) return;

        /* Update last stepping block, and execute attached commands. */
        player2lastSteppingBlockUUID.put(playerName, uuid);

        /* Trigger it. */
        ServerHelper.executeSync(() -> tryTriggerAttachmentModel(uuid, player, List.of(InteractType.STEP_ON)));
    }

    public static void testSteppingBlockForPlayers() {
        PlayerHelper.getOnlinePlayers().forEach(CommandAttachmentInitializer::testSteppingBlockForPlayer);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean existsAttachmentModel(@Nullable String uuid) {
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

    public static void tryTriggerAttachmentModel(@Nullable String uuid, PlayerEntity player, List<InteractType> receivedInteractTypes) {
        tryTriggerAttachmentModel(uuid,player,receivedInteractTypes, ()->{});
    }

    public static void tryTriggerAttachmentModel(@Nullable String uuid, PlayerEntity player, List<InteractType> receivedInteractTypes, Runnable postTriggered) {
        if (!CommandAttachmentInitializer.existsAttachmentModel(uuid)) return;
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
            ServerCommandSource source = CommandHelper.getCommandSource(player);
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

    @Document(id = 1751826433455L, value = "Attach one command to an item.")
    @CommandNode("attach-item-one")
    private static int $attachItemOne(@CommandSource ServerPlayerEntity player
        , @Document(id = 1751826436283L, value = "The interaction type to trigger this command.") Optional<InteractType> interactType
        , @Document(id = 1751826438227L, value = "Max use times of this command.") Optional<Integer> maxUseTimes
        , @Document(id = 1751826442468L, value = "Execute this command as who?") Optional<ExecuteAsType> executeAsType
        , @Document(id = 1751826444225L, value = "Should we destroy the item if the use times exceed.") Optional<Boolean> destroyItem
        , @Document(id = 1751826447371L, value = "The command.") GreedyString command
    ) {
        return CommandHelper.Pattern.itemInHandCommand(player.getCommandSource(), (thePlayer, mainHandStack) -> {
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
        });
    }

    @Document(id = 1751826450179L, value = "Attach one command to an entity.")
    @CommandNode("attach-entity-one")
    private static int $attachEntityOne(@CommandSource ServerPlayerEntity player
        , @Document(id = 1751826451977L, value = "The target entity.") Entity entity
        , @Document(id = 1751826454446L, value = "The interaction type to trigger this command.") Optional<InteractType> interactType
        , @Document(id = 1751826456136L, value = "Max use times of this command.") Optional<Integer> maxUseTimes
        , @Document(id = 1751826457661L, value = "Execute this command as who?") Optional<ExecuteAsType> executeAsType
        , @Document(id = 1751826459247L, value = "The command") GreedyString command
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

    @Document(id = 1751826465183L, value = "Attach one command to specified block.")
    @CommandNode("attach-block-one")
    private static int $attachBlockOne(@CommandSource ServerPlayerEntity player
        , BlockPos blockPos
        , @Document(id = 1751826466665L, value = "The interaction type to trigger this command.") Optional<InteractType> interactType
        , @Document(id = 1751826468188L, value = "Max use times of this command.") Optional<Integer> maxUseTimes
        , @Document(id = 1751826470533L, value = "Execute this command as who?") Optional<ExecuteAsType> executeAsType
        , @Document(id = 1751826472455L, value = "The command") GreedyString command
    ) {
        // get entity id
        String uuid = UuidHelper.getAttachedUuid(EntityHelper.getServerWorld(player), blockPos);
        CommandAttachmentModel model = getAttachmentModel(uuid);

        // new entry
        String $command = command.getValue();
        InteractType $interactType = interactType.orElse(InteractType.BOTH);
        ExecuteAsType $executeAsType = executeAsType.orElse(ExecuteAsType.FAKE_OP);
        Integer $maxUseTimes = maxUseTimes.orElse(Integer.MAX_VALUE);

        String createdIn = UuidHelper.toString(player.getWorld(), blockPos);
        model.getEntries().add(new BlockCommandAttachmentNode(createdIn, $command, $interactType, $executeAsType, $maxUseTimes, 0));

        // save model
        setAttachmentModel(uuid, model);
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751826477036L, value = "Detach all attached commands in the item.")
    @CommandNode("detach-item-all")
    private static int $detachItemAll(@CommandSource ServerPlayerEntity player) {
        return CommandHelper.Pattern.itemInHandCommand(player.getCommandSource(), (thePlayer, mainHandStack) -> {
            String uuid = UuidHelper.getOrSetAttachedUuid(mainHandStack);

            doDetachAttachment(uuid);
            return CommandHelper.Return.SUCCESS;
        });
    }

    @Document(id = 1751826478770L, value = "Detach all attached commands in the entity.")
    @CommandNode("detach-entity-all")
    private static int $detachEntityAll(@CommandSource ServerPlayerEntity player, Entity entity) {
        String uuid = entity.getUuidAsString();

        doDetachAttachment(uuid);
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751826482248L, value = "Detach all attached commands in the block.")
    @CommandNode("detach-block-all")
    private static int $detachBlockAll(@CommandSource ServerPlayerEntity player, BlockPos blockPos) {
        String uuid = UuidHelper.getAttachedUuid(EntityHelper.getServerWorld(player), blockPos);

        doDetachAttachment(uuid);
        return CommandHelper.Return.SUCCESS;
    }

    @SneakyThrows
    private static void doDetachAttachment(String uuid) {
        Managers.getAttachmentManager().unsetAttachment(COMMAND_ATTACHMENT_SUBJECT_NAME, uuid);
    }

    @Document(id = 1751826486559L, value = "Query all attached commands in the item.")
    @CommandNode("query-item")
    private static int $queryItem(@CommandSource ServerPlayerEntity player) {
        return CommandHelper.Pattern.itemInHandCommand(player.getCommandSource(), (thePlayer, mainHandStack) -> {
            String uuid = UuidHelper.getAttachedUuid(ItemStackHelper.Nbt.getNbt(mainHandStack));

            doQueryAttachment(player, uuid);
            return CommandHelper.Return.SUCCESS;
        });
    }

    @Document(id = 1751826488228L, value = "Query all attached commands in the entity.")
    @CommandNode("query-entity")
    private static int $queryEntity(@CommandSource ServerPlayerEntity player, Entity entity) {
        String uuid = entity.getUuidAsString();
        doQueryAttachment(player, uuid);
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751826492923L, value = "Query all attached commands in the block.")
    @CommandNode("query-block")
    private static int $queryBlock(@CommandSource ServerPlayerEntity player, BlockPos blockPos) {
        String uuid = UuidHelper.getAttachedUuid(EntityHelper.getServerWorld(player), blockPos);
        doQueryAttachment(player, uuid);
        return CommandHelper.Return.SUCCESS;
    }

    @SneakyThrows(IOException.class)
    private static void doQueryAttachment(ServerPlayerEntity player, String uuid) {
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

    @Override
    protected void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            TestSteppingOnBlockJob testSteppingOnBlockJob = new TestSteppingOnBlockJob();
            Managers.getScheduleManager().scheduleJob(testSteppingOnBlockJob);
        });
    }

    @Override
    protected void registerGsonTypeAdapter() {
        BaseConfigurationHandler.registerGsonTypeAdapter(CommandAttachmentNode.class, new CommandAttachmentNodeAdapter());
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

            throw new IllegalArgumentException("The type of command attachment entry is not supported!");
        }

    }
}

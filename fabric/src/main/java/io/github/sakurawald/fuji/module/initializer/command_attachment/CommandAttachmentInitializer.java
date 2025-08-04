package io.github.sakurawald.fuji.module.initializer.command_attachment;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.ItemStackHelper;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.EntityHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.UuidHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.GreedyString;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.event.impl.ServerLifecycleEvents;
import io.github.sakurawald.fuji.core.manager.Managers;
import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.command_attachment.command.argument.wrapper.ExecuteAsType;
import io.github.sakurawald.fuji.module.initializer.command_attachment.command.argument.wrapper.InteractType;
import io.github.sakurawald.fuji.module.initializer.command_attachment.config.adapter.CommandAttachmentNodeAdapter;
import io.github.sakurawald.fuji.module.initializer.command_attachment.config.model.CommandAttachmentDataModel;
import io.github.sakurawald.fuji.module.initializer.command_attachment.config.model.CommandAttachmentModel;
import io.github.sakurawald.fuji.module.initializer.command_attachment.job.TestSteppingOnBlockJob;
import io.github.sakurawald.fuji.module.initializer.command_attachment.service.CommandAttachmentService;
import io.github.sakurawald.fuji.module.initializer.command_attachment.structure.BlockCommandAttachmentNode;
import io.github.sakurawald.fuji.module.initializer.command_attachment.structure.CommandAttachmentNode;
import io.github.sakurawald.fuji.module.initializer.command_attachment.structure.EntityCommandAttachmentNode;
import io.github.sakurawald.fuji.module.initializer.command_attachment.structure.ItemStackCommandAttachmentNode;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

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

    public static final BaseConfigurationHandler<CommandAttachmentDataModel> data = new ObjectConfigurationHandler<>("command-attachment-data.json", CommandAttachmentDataModel.class)
        .enableAutoSaveFeature();

    @Document(id = 1751826433455L, value = "Attach one command to an item.")
    @CommandNode("attach-item-one")
    private static int $attachItemOne(@CommandSource ServerPlayerEntity player
        , Optional<InteractType> interactType
        , Optional<Integer> maxUseTimes
        , Optional<ExecuteAsType> executeAsType
        , Optional<Boolean> destroyItem
        , GreedyString command
    ) {
        return CommandHelper.Pattern.withItemInMainHand(player.getCommandSource(), (thePlayer, mainHandStack) -> {
            String uuid = UuidHelper.getOrSetAttachedUuid(mainHandStack);
            return CommandAttachmentService.withAttachmentDataNode(uuid, it -> {
                CommandAttachmentModel model = it.getEntries();

                /* Make new entry. */
                String $command = command.getValue();
                InteractType $interactType = interactType.orElse(InteractType.BOTH);
                ExecuteAsType $executeAsType = executeAsType.orElse(ExecuteAsType.FAKE_OP);
                Integer $maxUseTimes = maxUseTimes.orElse(Integer.MAX_VALUE);
                Boolean $destroyItem = destroyItem.orElse(true);
                ItemStackCommandAttachmentNode newEntry = new ItemStackCommandAttachmentNode($command, $interactType, $executeAsType, $maxUseTimes, 0, $destroyItem);

                /* Add the entry. */
                model.getEntries().add(newEntry);
                return CommandHelper.Return.SUCCESS;
            });
        });
    }

    @Document(id = 1751826450179L, value = "Attach one command to an entity.")
    @CommandNode("attach-entity-one")
    private static int $attachEntityOne(@CommandSource ServerPlayerEntity player
        , Entity entity
        , Optional<InteractType> interactType
        , Optional<Integer> maxUseTimes
        , Optional<ExecuteAsType> executeAsType
        , GreedyString command
    ) {
        String uuid = entity.getUuidAsString();
        return CommandAttachmentService.withAttachmentDataNode(uuid, it -> {
            CommandAttachmentModel model = it.getEntries();

            /* Make new entry. */
            String $command = command.getValue();
            InteractType $interactType = interactType.orElse(InteractType.BOTH);
            ExecuteAsType $executeAsType = executeAsType.orElse(ExecuteAsType.FAKE_OP);
            Integer $maxUseTimes = maxUseTimes.orElse(Integer.MAX_VALUE);
            EntityCommandAttachmentNode newEntry = new EntityCommandAttachmentNode($command, $interactType, $executeAsType, $maxUseTimes, 0);

            /* Add the entry. */
            model.getEntries().add(newEntry);
            return CommandHelper.Return.SUCCESS;
        });
    }

    @Document(id = 1751826465183L, value = "Attach one command to specified block.")
    @CommandNode("attach-block-one")
    private static int $attachBlockOne(@CommandSource ServerPlayerEntity player
        , BlockPos blockPos
        , Optional<InteractType> interactType
        , Optional<Integer> maxUseTimes
        , Optional<ExecuteAsType> executeAsType
        , GreedyString command
    ) {
        String uuid = UuidHelper.getAttachedUuid(EntityHelper.getServerWorld(player), blockPos);
        return CommandAttachmentService.withAttachmentDataNode(uuid, it -> {
            CommandAttachmentModel model = it.getEntries();

            /* Make the new entry. */
            String $command = command.getValue();
            InteractType $interactType = interactType.orElse(InteractType.BOTH);
            ExecuteAsType $executeAsType = executeAsType.orElse(ExecuteAsType.FAKE_OP);
            Integer $maxUseTimes = maxUseTimes.orElse(Integer.MAX_VALUE);
            String createdIn = UuidHelper.toString(player.getWorld(), blockPos);
            BlockCommandAttachmentNode newEntry = new BlockCommandAttachmentNode(createdIn, $command, $interactType, $executeAsType, $maxUseTimes, 0);

            // Add the entry.
            model.getEntries().add(newEntry);
            return CommandHelper.Return.SUCCESS;
        });
    }

    @Document(id = 1751826477036L, value = "Detach all attached commands in the item.")
    @CommandNode("detach-item-all")
    private static int $detachItemAll(@CommandSource ServerPlayerEntity player) {
        return CommandHelper.Pattern.withItemInMainHand(player.getCommandSource(), (thePlayer, mainHandStack) -> {
            String uuid = UuidHelper.getOrSetAttachedUuid(mainHandStack);
            CommandAttachmentService.detachAttachment(uuid);
            return CommandHelper.Return.SUCCESS;
        });
    }

    @Document(id = 1751826478770L, value = "Detach all attached commands in the entity.")
    @CommandNode("detach-entity-all")
    private static int $detachEntityAll(@CommandSource ServerPlayerEntity player, Entity entity) {
        String uuid = entity.getUuidAsString();
        CommandAttachmentService.detachAttachment(uuid);
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751826482248L, value = "Detach all attached commands in the block.")
    @CommandNode("detach-block-all")
    private static int $detachBlockAll(@CommandSource ServerPlayerEntity player, BlockPos blockPos) {
        String uuid = UuidHelper.getAttachedUuid(EntityHelper.getServerWorld(player), blockPos);
        CommandAttachmentService.detachAttachment(uuid);
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751826486559L, value = "Query all attached commands in the item.")
    @CommandNode("query-item")
    private static int $queryItem(@CommandSource ServerPlayerEntity player) {
        return CommandHelper.Pattern.withItemInMainHand(player.getCommandSource(), (thePlayer, mainHandStack) -> {
            String uuid = UuidHelper.getAttachedUuid(ItemStackHelper.CustomData.getCustomDataNbt(mainHandStack));
            return CommandAttachmentService.queryAttachment(player.getCommandSource(), uuid);
        });
    }

    @Document(id = 1751826488228L, value = "Query all attached commands in the entity.")
    @CommandNode("query-entity")
    private static int $queryEntity(@CommandSource ServerCommandSource source, Entity entity) {
        String uuid = entity.getUuidAsString();
        return CommandAttachmentService.queryAttachment(source, uuid);
    }

    @Document(id = 1751826492923L, value = "Query all attached commands in the block.")
    @CommandNode("query-block")
    private static int $queryBlock(@CommandSource ServerCommandSource source, BlockPos blockPos) {
        String uuid = UuidHelper.getAttachedUuid(source.getWorld(), blockPos);
        return CommandAttachmentService.queryAttachment(source, uuid);
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

}

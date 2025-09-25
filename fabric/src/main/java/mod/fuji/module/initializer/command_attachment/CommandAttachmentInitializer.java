package mod.fuji.module.initializer.command_attachment;

import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.EntityHelper;
import mod.fuji.core.auxiliary.minecraft.ItemStackHelper;
import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.auxiliary.minecraft.UuidHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandRequirement;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.core.command.argument.wrapper.impl.GreedyString;
import mod.fuji.core.config.handler.abst.BaseConfigurationHandler;
import mod.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import mod.fuji.core.config.mapper.GsonMapper;
import mod.fuji.core.document.annotation.ColorBox;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.event.annotation.EventConsumer;
import mod.fuji.core.event.message.player.PlayerActionEvent;
import mod.fuji.core.event.message.player.PlayerBlockBreakPreEvent;
import mod.fuji.core.event.message.player.PlayerInteractBlockPreEvent;
import mod.fuji.core.event.message.player.PlayerInteractEntityPreEvent;
import mod.fuji.core.event.message.player.PlayerInteractItemPreEvent;
import mod.fuji.module.initializer.ModuleInitializer;
import mod.fuji.module.initializer.command_attachment.command.argument.wrapper.ExecuteAsType;
import mod.fuji.module.initializer.command_attachment.command.argument.wrapper.InteractType;
import mod.fuji.module.initializer.command_attachment.config.adapter.CommandAttachmentEntryAdapter;
import mod.fuji.module.initializer.command_attachment.config.model.CommandAttachmentConfigModel;
import mod.fuji.module.initializer.command_attachment.config.model.CommandAttachmentDataModel;
import mod.fuji.module.initializer.command_attachment.config.transformer.CommandAttachmentV1SchemaTransformer;
import mod.fuji.module.initializer.command_attachment.gui.CommandAttachmentEditorGui;
import mod.fuji.module.initializer.command_attachment.job.TestSteppingOnBlockJob;
import mod.fuji.module.initializer.command_attachment.service.CommandAttachmentService;
import mod.fuji.module.initializer.command_attachment.structure.CommandAttachments;
import mod.fuji.module.initializer.command_attachment.structure.attachment_entry.BaseCommandAttachmentEntry;
import mod.fuji.module.initializer.command_attachment.structure.attachment_entry.BlockCommandAttachmentEntry;
import mod.fuji.module.initializer.command_attachment.structure.attachment_entry.EntityCommandAttachmentEntry;
import mod.fuji.module.initializer.command_attachment.structure.attachment_entry.ItemStackCommandAttachmentEntry;
import java.util.List;
import java.util.Optional;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

@Document(id = 1751826430284L, value = """
    This module allows `attaching` commands to various `objects`:
    - Item Stacks
    - Blocks
    - Entities
    - ...
    """)
@ColorBox(id = 1751870462624L, color = ColorBox.ColorBoxTypes.NOTE, value = """
    ◉ How it works?
    When you attach commands into an item.
    We will save a `binding ID` in the item NBT.
    Every item that has the same `binding ID` in its NBT data, shares the same `binding commands instance`.
    """)
@ColorBox(id = 1756447342634L, color = ColorBox.ColorBoxTypes.NOTE, value = """
    ◉ Valid `interaction types` for each `attached types`.
    - `item`: `LEFT_CLICK`, `RIGHT_CLICK`, `ANY_CLICK`, `SWAP_HAND`
    - `block`: `LEFT_CLICK`, `RIGHT_CLICK`, `ANY_CLICK`, `STEP_ON`
    - `entity`: `RIGHT_CLICK`, `ANY_CLICK`
    """)
@ColorBox(id = 1751870464919L, color = ColorBox.ColorBoxTypes.TIP, value = """
    ◉ Give the `attached item` to players.
    You can use `kit` module.
    The kit module will save the item NBT.
    So if you define a kit, puts `the attached item` inside it.
    Then give the kit to a player.
    The player will get the magic item.
    """)
@ColorBox(id = 1756460500752L, color = ColorBox.ColorBoxTypes.TIP, value = """
    ◉ Integrate with `command_cooldown` module.
    You can use `command_cooldown` module to create a `named cooldown`.
    The `named cooldown` can restrict the `use interval` and `use times` of a specified command.
    And then, attach the `named cooldown test command` to an `item` using `command_attachment` module.
    """)
@ColorBox(id = 1756458386657L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ Make a magic-stick, which heals the player on clicked.
    Issue `/command-attachment attach-item-one heal`

    ◉ Make a magic-stick, which:
    1. Gives diamonds * 1 on left clicked. (With use limit 3)
    2. Gives gold_ingot *1 on right clicked. (With use limit 5)

    Issue the following commands:
    1. `/command-attachment attach-item-one --maxUseTimes 3 --interactType LEFT_CLICK give %player:name% minecraft:diamond 1`
    2. `/command-attachment attach-item-one --maxUseTimes 5 --interactType RIGHT_CLICK give %player:name% minecraft:gold_ingot 1`

    ◉ Make a magic-stick, which gives apple * 1, with use limit 3 times, and keep the item without destroying it.
    Issue: `/command-attachment attach-item-one --maxUseTimes 3 --destroyItem false give %player:name% minecraft:apple 1`.

    ◉ Query the attached commands in a specific item.
    Issue `/command-attachment query-item`.

    ◉ Let an entity say hello on right clicked.
    Issue: `/command-attachment attach-entity-one \\<entity-id\\> say hello %player:name%`

    ◉ Make a portal block.
    Issue: `/command-attachment attach-block-one 0 0 0 --interactType STEP_ON tppos --targetPlayer %player:name% --dimension minecraft:the_end --x 0 --y 66 --z 0 %player:name%`.

    ◉ View and edit the attached object.
    Issue: `/command-attachment editor`
    """)


@CommandNode("command-attachment")
@CommandRequirement(level = 4)
public class CommandAttachmentInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<CommandAttachmentConfigModel> config = ObjectConfigurationHandler.ofModule(BaseConfigurationHandler.CONFIG_JSON_LITERAL, CommandAttachmentConfigModel.class);

    public static final BaseConfigurationHandler<CommandAttachmentDataModel> data = ObjectConfigurationHandler
        .ofModule("command-attachment-data.json", CommandAttachmentDataModel.class)
        .enableAutoSaveFeature()
        .installTransformer(new CommandAttachmentV1SchemaTransformer());

    @Document(id = 1751826433455L, value = "Attach one command to an item.")
    @CommandNode("attach-item-one")
    private static int $attachItemOne(@CommandSource ServerPlayerEntity player
        , Optional<InteractType> interactType
        , Optional<Integer> maxUseTimes
        , Optional<ExecuteAsType> executeAsType
        , Optional<Boolean> destroyItem
        , GreedyString command
    ) {
        return CommandHelper.Pattern.withItemInMainHandCommand(player.getCommandSource(), (thePlayer, mainHandStack) -> {
            String uuid = UuidHelper.getOrSetAttachedUuid(mainHandStack);
            return CommandAttachmentService.withAttachmentDataNode(uuid, it -> {
                CommandAttachments model = it.getAttachments();

                /* Make new entry. */
                String $command = command.getValue();
                InteractType $interactType = interactType.orElse(InteractType.ANY_CLICK);
                ExecuteAsType $executeAsType = executeAsType.orElse(ExecuteAsType.FAKE_OP);
                Integer $maxUseTimes = maxUseTimes.orElse(Integer.MAX_VALUE);
                Boolean $destroyItem = destroyItem.orElse(true);
                ItemStackCommandAttachmentEntry newEntry = new ItemStackCommandAttachmentEntry($command, $interactType, $executeAsType, $maxUseTimes, 0, $destroyItem);

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
        String uuid = UuidHelper.getAttachedUuid(entity);
        return CommandAttachmentService.withAttachmentDataNode(uuid, it -> {
            CommandAttachments model = it.getAttachments();

            /* Make new entry. */
            String $command = command.getValue();
            InteractType $interactType = interactType.orElse(InteractType.ANY_CLICK);
            ExecuteAsType $executeAsType = executeAsType.orElse(ExecuteAsType.FAKE_OP);
            Integer $maxUseTimes = maxUseTimes.orElse(Integer.MAX_VALUE);
            EntityCommandAttachmentEntry newEntry = new EntityCommandAttachmentEntry($command, $interactType, $executeAsType, $maxUseTimes, 0, false);

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
            CommandAttachments model = it.getAttachments();

            /* Make the new entry. */
            String $command = command.getValue();
            InteractType $interactType = interactType.orElse(InteractType.ANY_CLICK);
            ExecuteAsType $executeAsType = executeAsType.orElse(ExecuteAsType.FAKE_OP);
            Integer $maxUseTimes = maxUseTimes.orElse(Integer.MAX_VALUE);
            String createdIn = UuidHelper.toString(PlayerHelper.getServerWorld(player), blockPos);
            BlockCommandAttachmentEntry newEntry = new BlockCommandAttachmentEntry(createdIn, $command, $interactType, $executeAsType, $maxUseTimes, 0, false);

            // Add the entry.
            model.getEntries().add(newEntry);
            return CommandHelper.Return.SUCCESS;
        });
    }

    @SuppressWarnings("CodeBlock2Expr")
    @Document(id = 1751826477036L, value = "Detach all attached commands in the item.")
    @CommandNode("detach-item-all")
    private static int $detachItemAll(@CommandSource ServerPlayerEntity player, Optional<Boolean> confirm) {
        return CommandHelper.Pattern.withCommandConfirmed(player, confirm, () -> {
            return CommandHelper.Pattern.withItemInMainHandCommand(player.getCommandSource(), (thePlayer, mainHandStack) -> {
                String uuid = UuidHelper.getOrSetAttachedUuid(mainHandStack);
                CommandAttachmentService.removeAttachmentDataNode(uuid);
                return CommandHelper.Return.SUCCESS;
            });
        });
    }

    @Document(id = 1751826478770L, value = "Detach all attached commands in the entity.")
    @CommandNode("detach-entity-all")
    private static int $detachEntityAll(@CommandSource ServerPlayerEntity player, Entity entity, Optional<Boolean> confirm) {
        return CommandHelper.Pattern.withCommandConfirmed(player, confirm, () -> {
            String uuid = UuidHelper.getAttachedUuid(entity);
            CommandAttachmentService.removeAttachmentDataNode(uuid);
            return CommandHelper.Return.SUCCESS;
        });
    }

    @Document(id = 1751826482248L, value = "Detach all attached commands in the block.")
    @CommandNode("detach-block-all")
    private static int $detachBlockAll(@CommandSource ServerPlayerEntity player, BlockPos blockPos, Optional<Boolean> confirm) {
        return CommandHelper.Pattern.withCommandConfirmed(player, confirm, () -> {
            String uuid = UuidHelper.getAttachedUuid(EntityHelper.getServerWorld(player), blockPos);
            CommandAttachmentService.removeAttachmentDataNode(uuid);
            return CommandHelper.Return.SUCCESS;
        });
    }

    @Document(id = 1751826486559L, value = "Query all attached commands in the item.")
    @CommandNode("query-item")
    private static int $queryItem(@CommandSource ServerPlayerEntity player) {
        return CommandHelper.Pattern.withItemInMainHandCommand(player.getCommandSource(), (thePlayer, mainHandStack) -> {
            Optional<String> uuid = UuidHelper.getAttachedUuid(mainHandStack);
            return CommandAttachmentService.printAttachmentDataNode(player.getCommandSource(), uuid);
        });
    }

    @Document(id = 1751826488228L, value = "Query all attached commands in the entity.")
    @CommandNode("query-entity")
    private static int $queryEntity(@CommandSource ServerCommandSource source, Entity entity) {
        String uuid = UuidHelper.getAttachedUuid(entity);
        return CommandAttachmentService.printAttachmentDataNode(source, Optional.of(uuid));
    }

    @Document(id = 1751826492923L, value = "Query all attached commands in the block.")
    @CommandNode("query-block")
    private static int $queryBlock(@CommandSource ServerCommandSource source, BlockPos blockPos) {
        String uuid = UuidHelper.getAttachedUuid(source.getWorld(), blockPos);
        return CommandAttachmentService.printAttachmentDataNode(source, Optional.of(uuid));
    }

    @Document(id = 1756452396077L, value = "Open the command attachment editor.")
    @CommandNode("editor")
    private static int $editor(@CommandSource ServerPlayerEntity player) {
        CommandAttachmentEditorGui
            .make(player)
            .open();
        return CommandHelper.Return.SUCCESS;
    }

    @Override
    protected void registerGsonTypeAdapters() {
        GsonMapper.registerGsonTypeAdapter(BaseCommandAttachmentEntry.class, new CommandAttachmentEntryAdapter());
    }

    @EventConsumer
    private static void consumePlayerActionEvent(PlayerActionEvent event) {
        PlayerActionC2SPacket packet = event.getPacket();
        if (packet.getAction() == PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND) {
            ServerPlayerEntity player = event.getPlayer();
            ItemStack itemStack = player.getMainHandStack();
            UuidHelper
                .getAttachedUuid(ItemStackHelper.CustomData.getCustomDataNbt(itemStack))
                .ifPresent($uuid -> {
                    CommandAttachmentService.tryTriggerAttachmentDataNode($uuid, player, List.of(InteractType.SWAP_HAND), () -> event.getCallbackInfo().cancel());
                });

        }
    }

    @EventConsumer
    private static void consumePlayerBlockBreakPreEvent(PlayerBlockBreakPreEvent event) {
        if (event.getCallbackInfoReturnable().isCancelled()) return;

        ServerPlayerEntity player = event.getPlayer();
        ServerWorld world = EntityHelper.getServerWorld(player);
        String uuid = UuidHelper.getAttachedUuid(world, event.getBlockPos());
        CommandAttachmentService
            .findAttachmentDataNode(uuid)
            .ifPresent(it -> {
                event.getCallbackInfoReturnable().setReturnValue(false);
                TextHelper.sendTextByKey(player, "command_attachment.protect");
            });
    }

    @EventConsumer
    private static void consumePlayerInteractItemPreEvent(PlayerInteractItemPreEvent event) {
        if (event.getCallbackInfoReturnable().isCancelled()) return;

        UuidHelper
            .getAttachedUuid(ItemStackHelper.CustomData.getCustomDataNbt(event.getItemStack()))
            .ifPresent($uuid -> {
                CommandAttachmentService.tryTriggerAttachmentDataNode($uuid, event.getPlayer(), List.of(InteractType.RIGHT_CLICK, InteractType.ANY_CLICK), () -> {});
            });
    }

    @EventConsumer(injectorPriority = EventConsumer.LOWER, consumerPriority = EventConsumer.LOWEST)
    private static void consumePlayerInteractBlockPreEvent(PlayerInteractBlockPreEvent event) {
        if (event.getCallbackInfoReturnable().isCancelled()) return;

        if (event.getHand() == Hand.MAIN_HAND) {
            String uuid = UuidHelper.getAttachedUuid(event.getWorld(), event.getBlockHitResult().getBlockPos());
            CommandAttachmentService.tryTriggerAttachmentDataNode(uuid, event.getPlayer(), List.of(InteractType.RIGHT_CLICK, InteractType.ANY_CLICK), () -> {
                // Cancel the action if the target block contains attached commands.
                event.getCallbackInfoReturnable().setReturnValue(ActionResult.FAIL);
            });
        }
    }

    @EventConsumer(injectorPriority = EventConsumer.LOWER, consumerPriority = EventConsumer.LOWEST)
    private static void consumePlayerInteractEntityPreEvent(PlayerInteractEntityPreEvent event) {
        if (event.getCallbackInfoReturnable().isCancelled()) return;

        if (event.getHand() == Hand.MAIN_HAND) {
            String uuid = event.getEntity().getUuidAsString();
            CommandAttachmentService.tryTriggerAttachmentDataNode(uuid, event.getPlayer(), List.of(InteractType.RIGHT_CLICK, InteractType.ANY_CLICK), () -> {});
        }
    }

}

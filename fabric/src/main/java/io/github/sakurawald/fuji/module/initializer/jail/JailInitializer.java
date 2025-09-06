package io.github.sakurawald.fuji.module.initializer.jail;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.command.annotation.CommandTarget;
import io.github.sakurawald.fuji.core.event.annotation.EventConsumer;
import io.github.sakurawald.fuji.core.event.message.impl.on_demand.ModifyPlayerDisplayNameEvent;
import io.github.sakurawald.fuji.core.event.message.impl.on_demand.ModifyPlayerListNameEvent;
import io.github.sakurawald.fuji.core.service.duration_parser.command.argument.wrapper.Duration;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.GreedyString;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.OfflinePlayerName;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.event.message.impl.ServerLifecycleEvents;
import io.github.sakurawald.fuji.core.manager.Managers;
import io.github.sakurawald.fuji.core.structure.GlobalPos;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.jail.command.argument.wrapper.JailedPlayerName;
import io.github.sakurawald.fuji.module.initializer.jail.config.model.JailConfigModel;
import io.github.sakurawald.fuji.module.initializer.jail.config.model.JailDataModel;
import io.github.sakurawald.fuji.module.initializer.jail.gui.JailListGui;
import io.github.sakurawald.fuji.module.initializer.jail.job.PatrolJailJob;
import io.github.sakurawald.fuji.module.initializer.jail.job.UpdateJailRecordsJob;
import io.github.sakurawald.fuji.module.initializer.jail.service.JailService;
import io.github.sakurawald.fuji.module.initializer.jail.structure.JailDescriptor;
import java.util.Optional;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

@Document(id = 1753681022357L, value = """
    This module allows you to define a `jail`.
    It can be used to `punish` a player with bad behaviour, without `banning` it.
    """)
@ColorBox(id = 1753757093710L, color = ColorBox.ColorBoxTypes.NOTE, value = """
    ◉ How it works?
    1. Each `jail descriptor` is used to define a `jail` instance.
    1.a. You can `create` a descriptor using the `/jail create` command.
    1.b. You can `delete` a descriptor using the `/jail delete` command.
    1.c. You can `list` all created descriptors using the `/jail list` command.
    2. Each `jail` can hold `more than 1 player`.
    2.a. Each `jailed player` is recorded with a `jail record`.
    2.b. A `player` can only be put in one `jail` at a time.
    3. Each `jail` has a `position` property.
    3.a. It's `initialized` to your current position when you run the `/jail create` command.
    3.b. You can set a new position for it using `/jail set-position` command.
    3.c. You can teleport to the position using `/jail tp` command.
    4. You can `put` a `player` into a `jail`, or `un-put` it.
    4.a. To `put`, use `/jail put` command.
    4.b. To `un-put`, use `/jail un-put` command.
    4.c. To query the info, use `/jail where` command.

    ◉ The difference between `banned players` and `jailed players`.
    1. For a `banned players`: They can't `join` the server.
    2. For a `jailed players`: They can `join` the server.

    <green> NOTE: A `jail` is only used to hold information.
    <green> You need to write `punishment commands` in `onJailedEvent` and `onUnjailedEvent`.
    <green> You can also write `patrol commands` to check and restrict the actions of the jailed players.
    """)
@ColorBox(id = 1753774841738L, color = ColorBox.ColorBoxTypes.NOTE, value = """
    ◉ The `placeholders` to the `position` of a `jail`.
    1. `%fuji:jail_dimension%`
    2. `%fuji:jail_x%`
    3. `%fuji:jail_y%`
    4. `%fuji:jail_z%`
    5. `%fuji:jail_yaw%`
    6. `%fuji:jail_pitch%`

    ◉ Restrict the `movement` of `jailed players` within a specified area.
    With the help of `position placeholders`, you can write `patrol commands` to restrict movements.
    You can define commands to restrict the movement of jailed players within a specified area.
    1. `/execute as %player:name% at @s unless dimension %fuji:jail_dimension% run execute in %fuji:jail_dimension% run tp @s %fuji:jail_x% %fuji:jail_y% %fuji:jail_z%`
    2. `/execute as %player:name% if entity @s[x=%fuji:jail_x%,y=%fuji:jail_y%,z=%fuji:jail_z%,distance=8..] run tp @s %fuji:jail_x% %fuji:jail_y% %fuji:jail_z%`

    <green>NOTE: If you have enabled the `teleport_warmup` module, remember to assign the `warmup bypass` permission for the `jailed` user group.
    <green>So that `jailed players` can be `instantly` teleported back to the `position of the jail`.
    1. `/lp group jailed permission set fuji.teleport_warmup.bypass`

    <green>NOTE: To disable the `vanilla Minecraft command feedbacks`, you can issue:
    1. `/gamerule sendCommandFeedback false`
    """)
@ColorBox(id = 1753750852480L, color = ColorBox.ColorBoxTypes.TIPS, value = """
    ◉ Understand the `execution time` of a `command`.
    Some commands require the `target player` online to work.
    For example, the `/send-message %player:name% You are jailed.` didn't work if the target player is `off-line`.
    In this case, you can use the `command_meta.when_online` module, to submit and schedule a command.
    Issue: `/when-online %player:name% send-message %player:name% You are jailed.`

    ◉ Create a `luckperms group` for `jailed players`.
    You can create a `luckperms group` for `jailed players`.
    And assign the `positive permissions` and `negative permissions` to `jailed players`.
    Use the `permissions` to control the behaviours of `jailed players`.

    ◉ Restrict the actions of `jailed players`.
    You can integrate with the `anti_build` module.
    To assign `negative permissions` to `jailed user group`, to limit the actions of them.
    Issue:
    1. `/lp group jailed permission set fuji.anti_build.break_block.override.* false`
    2. `/lp group jailed permission set fuji.anti_build.place_block.override.* false`
    3. `/lp group jailed permission set fuji.anti_build.interact_item.override.* false`
    4. `/lp group jailed permission set fuji.anti_build.interact_entity.override.* false`
    5. `/lp group jailed permission set fuji.anti_build.interact_block.override.* false`
    <green>NOTE: You need to enable the `wildcard permission` feature in `luckperms` mod config.
    """)
@ColorBox(id = 1754329012853L, color = ColorBox.ColorBoxTypes.TIPS, value = """
    ◉ Restrict `jailed players` to using only allowed commands.
    You can integrate with `command_permission` module.
    Issue:
    1. `/lp group jailed permission set fuji.permission.* false`
    Dis-allow to use `all commands`.

    2. `/lp group jailed permission set fuji.permission.back true`
    Allow to use the `/back` command.
    """)
@ColorBox(id = 1753780761908L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ Create a `jail` in your current position.
    Issue: `/jail create \\<jail-id\\>`

    ◉ Set the position of a jail to your current position.
    Issue: `/jail set-position \\<jail-id\\>`

    ◉ Teleport to the position of a jail.
    Issue: `/jail tp \\<jail-id\\>`

    ◉ List all created jails.
    Issue: `/jail list`

    ◉ Put a player into a jail.
    Issue:
    1. `/jail put Steve \\<jail-id\\> Steal items.`
    2. `/jail put Steve \\<jail-id\\> --duration 1s2m3h4d5w6M7y Steal items.`

    ◉ Un-put a player from the jail.
    Issue: `/jail unput Steve`

    ◉ Query which jail a player is in.
    Issue: `/jail where Steve`
    """)
public class JailInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<JailConfigModel> config = ObjectConfigurationHandler.ofModule(BaseConfigurationHandler.CONFIG_JSON_LITERAL, JailConfigModel.class);

    public static final BaseConfigurationHandler<JailDataModel> data = ObjectConfigurationHandler
        .ofModule("jail-data.json", JailDataModel.class)
        .enableAutoSaveFeature();

    @Document(id = 1753686048373L, value = "List all defined `jails`.")
    @CommandNode("jail list")
    @CommandRequirement(level = 4)
    private static int $list(@CommandSource ServerCommandSource source) {
        TextHelper.sendTextByKey(source, "jail.list", JailService.getJailIds());
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1753686063844L, value = "Put the `player` into a specified `jail`.")
    @CommandNode("jail put")
    @CommandRequirement(level = 4)
    private static int $put(@CommandSource ServerCommandSource source, OfflinePlayerName playerName, JailDescriptor jail, Optional<Duration> duration, GreedyString reason) {
        String creatorName = source.getName();
        String $playerName = playerName.getValue();
        String $reason = reason.getValue();
        String $duration = duration
            .orElseGet(() -> new Duration(jail.getDefaultJailedDuration()))
            .getValue();

        return JailService
            .getActiveJailRecord($playerName)
            .map(activeJailRecord -> {
                TextHelper.sendTextByKey(source, "jail.already_in_jail", $playerName, jail.getId());
                return CommandHelper.Return.FAILURE;
            })
            .orElseGet(() -> {
                try {
                    JailService.createJailRecord(creatorName, $playerName, jail, $reason, $duration);
                    TextHelper.sendTextByKey(source, "jail.put", $playerName, jail.getId());
                } catch (Exception e) {
                    TextHelper.sendTextByKey(source, "jail.put.failed", $playerName);
                    JailService.deactivateJailRecordWithoutEvents($playerName);
                }
                return CommandHelper.Return.SUCCESS;
            });
    }

    @Document(id = 1753690598413L, value = "Remove a player from the jail it is currently in.")
    @CommandNode("jail un-put")
    @CommandRequirement(level = 4)
    private static int $unPut(@CommandSource ServerCommandSource source, JailedPlayerName playerName) {
        String $playerName = playerName.getValue();

        return JailService
            .getActiveJailRecord($playerName)
            .map(activeJailRecord -> {
                JailService.deactivateJailRecord(activeJailRecord);
                TextHelper.sendTextByKey(source, "jail.unput", $playerName, activeJailRecord.getOwnerJailDescriptor().getId());
                return CommandHelper.Return.SUCCESS;
            })
            .orElseGet(() -> {
                TextHelper.sendTextByKey(source, "jail.not_in_jail", $playerName);
                return CommandHelper.Return.FAILURE;
            });
    }

    @Document(id = 1753692574518L, value = "Find the `jail` the player is in.")
    @CommandNode("jail where")
    @CommandRequirement(level = 4)
    private static int $where(@CommandSource ServerCommandSource source, JailedPlayerName playerName) {
        String $playerName = playerName.getValue();
        return JailService
            .getActiveJailRecord($playerName)
            .map(jailRecord -> {
                JailDescriptor ownerJailDescriptor = jailRecord.getOwnerJailDescriptor();
                TextHelper.sendTextByKey(source, "line.separator");
                TextHelper.sendTextByKey(source, "jail.record.prisoner_name", $playerName);
                TextHelper.sendTextByKey(source,"jail.record.creator_name", jailRecord.getCreatorName());
                TextHelper.sendTextByKey(source,"jail.record.created_time", jailRecord.getFormattedCreatedTimestamp());
                TextHelper.sendTextByKey(source, "jail.record.jail_id", ownerJailDescriptor.getId());
                TextHelper.sendTextByKey(source,"jail.record.specified_jail_duration", jailRecord.getSpecifiedJailDuration());
                TextHelper.sendTextByKey(source, "jail.record.remaining_jail_duration", jailRecord.getRemainingJailDuration());
                TextHelper.sendTextByKey(source, "jail.record.reason", TextHelper.Parsers.escapeTags(jailRecord.getReason()));
                return CommandHelper.Return.SUCCESS;
            })
            .orElseGet(() -> {
                TextHelper.sendTextByKey(source, "jail.not_in_jail", $playerName);
                return CommandHelper.Return.FAILURE;
            });
    }

    @Document(id = 1753771412507L, value = "A predicate command to check if the target player is jailed.")
    @CommandNode("is-jailed?")
    @CommandRequirement(level = 4)
    private static int $isJailed(@CommandSource ServerCommandSource source, String playerName) {
        return CommandHelper.Return.returnBoolean(source, JailService.getActiveJailRecord(playerName).isPresent());
    }

    @Document(id = 1753772112918L, value = "Create a new `jail` descriptor.")
    @CommandNode("jail create")
    @CommandRequirement(level = 4)
    private static int $create(@CommandSource ServerCommandSource source, String jailId) {
        return JailService.findJailDescriptor(jailId)
            .map(it -> {
                TextHelper.sendTextByKey(source,"jail.already_exists", jailId);
                return CommandHelper.Return.FAILURE;
            })
            .orElseGet(() -> {
                JailService.createJailDescriptor(jailId, source);
                TextHelper.sendTextByKey(source, "jail.create.success", jailId);
                return CommandHelper.Return.SUCCESS;
            });
    }

    @Document(id = 1753772960860L, value = "Delete an existing `jail` descriptor")
    @CommandNode("jail delete")
    @CommandRequirement(level = 4)
    private static int $deleteJail(@CommandSource ServerCommandSource source, JailDescriptor jail, Optional<Boolean> confirm) {
        return CommandHelper.Pattern.withCommandConfirmed(source, confirm, () -> {
            JailService.deleteJailDescriptor(jail);
            TextHelper.sendTextByKey(source, "jail.delete.success", jail.getId());
            return CommandHelper.Return.SUCCESS;
        });
    }

    @Document(id = 1753774327312L, value = "Teleport to the `position` of an existing `jail`.")
    @CommandNode("jail tp")
    @CommandRequirement(level = 4)
    private static int $tp(@CommandSource @CommandTarget ServerPlayerEntity player, JailDescriptor jail) {
        jail.getGlobalPosition().teleport(player);
        TextHelper.sendTextByKey(player, "jail.tp", jail.getId());
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1753774502626L, value = "Set the `position` of the specified `jail` to your current position.")
    @CommandNode("jail set-position")
    @CommandRequirement(level = 4)
    private static int $setPosition(@CommandSource ServerPlayerEntity player, JailDescriptor jail) {
        GlobalPos newValue = GlobalPos.of(player);
        JailService.setJailPosition(jail, newValue);
        TextHelper.sendTextByKey(player,"jail.set_position", jail.getId());
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1753864337061L, value = "Open the jail GUI.")
    @CommandNode("jail gui")
    @CommandRequirement(level = 4)
    private static int $gui(@CommandSource ServerPlayerEntity player) {
        JailListGui
            .make(player)
            .open();
        return CommandHelper.Return.SUCCESS;
    }


    @Override
    protected void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            UpdateJailRecordsJob updateJailRecordsJob = new UpdateJailRecordsJob();
            Managers.getScheduleManager().scheduleJob(updateJailRecordsJob);
        });

        PatrolJailJob.reloadPatrolJobs();
    }

    @Override
    protected void onReload() {
        PatrolJailJob.reloadPatrolJobs();
    }

    @Override
    protected void registerPlaceholders() {
        JailPlaceholders.registerJailIdPlaceholder();
        JailPlaceholders.registerJailDisplayNamePlaceholder();
        JailPlaceholders.registerJailCreatorNamePlaceholder();
        JailPlaceholders.registerJailCreatedDatePlaceholder();
        JailPlaceholders.registerJailSpecifiedJailDurationPlaceholder();
        JailPlaceholders.registerJailRemainingJailDurationPlaceholder();
        JailPlaceholders.registerJailReasonPlaceholder();

        JailPlaceholders.registerJailDimensionPlaceholder();
        JailPlaceholders.registerJailXPlaceholder();
        JailPlaceholders.registerJailYPlaceholder();
        JailPlaceholders.registerJailZPlaceholder();
        JailPlaceholders.registerJailYawPlaceholder();
        JailPlaceholders.registerJailPitchPlaceholder();
    }

    @EventConsumer(injectorPriority = EventConsumer.HIGHEST)
    private static void modifyPlayerListName(ModifyPlayerListNameEvent event) {
        ServerPlayerEntity player = event.getPlayer();
        Text original = event.getText();
        Text newValue = JailService.modifyDisplayName(original, player);
        event.setText(newValue);
    }

    @EventConsumer(injectorPriority = EventConsumer.HIGHEST)
    private static void modifyPlayerDisplayName(ModifyPlayerDisplayNameEvent event) {
        PlayerEntity player = event.getPlayer();
        Text original = event.getText();
        Text newValue = JailService.modifyDisplayName(original, player);
        event.setText(newValue);
    }

}

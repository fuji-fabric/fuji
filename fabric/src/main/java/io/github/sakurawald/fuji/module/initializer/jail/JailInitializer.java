package io.github.sakurawald.fuji.module.initializer.jail;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.GreedyString;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.OfflinePlayerName;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.event.impl.ServerLifecycleEvents;
import io.github.sakurawald.fuji.core.manager.Managers;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.jail.command.argument.wrapper.JailedPlayerName;
import io.github.sakurawald.fuji.module.initializer.jail.config.model.JailConfigModel;
import io.github.sakurawald.fuji.module.initializer.jail.config.model.JailDataModel;
import io.github.sakurawald.fuji.module.initializer.jail.job.PatrolJailJob;
import io.github.sakurawald.fuji.module.initializer.jail.job.UpdateJailRecordsJob;
import io.github.sakurawald.fuji.module.initializer.jail.service.JailService;
import io.github.sakurawald.fuji.module.initializer.jail.structure.JailDescriptor;
import java.util.Optional;
import net.minecraft.server.command.ServerCommandSource;

@Document(id = 1753681022357L, value = """
    This module allows you to define a `jail`.
    It can be used to `punish` a player with bad behaviour, without `banning` it.
    """)
@ColorBox(id = 1753757093710L, color = ColorBox.ColorBlockTypes.NOTE, value = """
    ◉ How it works?
    1. You can define a `jail` by configuring the `jail descriptors` in the config file.
    2. For each `jail`,

    """)
@ColorBox(id = 1753750852480L, color = ColorBox.ColorBlockTypes.TIPS, value = """
    ◉ Understand the `execution time` of a `command`.
    Some commands require the `target player` online to work.
    For example, the `/send-message %player:name% You are jailed.` didn't work if the target player is `off-line`.
    In this case, you can use the `command_meta.when_online` module, to submit and schedule a command.
    Issue: `/when-online %player:name% send-message %player:name% You are jailed.`

    ◉ Create a `luckperms group` for `jailed players`.
    You can create a `luckperms group` for `jailed players`.
    And assign the `positive permissions` and `negative permissions` to `jailed players`.
    Use the `permissions` to control the behaviours of `jailed players`.

    ◉ Limit the actions of `jailed players`.
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
public class JailInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<JailConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, JailConfigModel.class);

    public static final BaseConfigurationHandler<JailDataModel> data = new ObjectConfigurationHandler<>("jail-data.json", JailDataModel.class)
        .setAutoSaveEveryMinute();

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
    private static int $put(@CommandSource ServerCommandSource source, OfflinePlayerName player, JailDescriptor jail, Optional<String> duration, GreedyString reason) {
        String creatorName = source.getName();
        String playerName = player.getValue();
        String $reason = reason.getValue();
        String $duration = duration.orElseGet(jail::getDefaultJailedDuration);

        return JailService
            .getCurrentJailRecord(playerName)
            .map(currentJailRecord -> {
                TextHelper.sendTextByKey(source, "jail.already_in_jail", playerName, jail.getId());
                return CommandHelper.Return.FAIL;
            })
            .orElseGet(() -> {
                try {
                    JailService.createJailRecord(creatorName, playerName, jail, $reason, $duration);
                    TextHelper.sendTextByKey(source, "jail.put", playerName, jail.getId());
                } catch (Exception e) {
                    TextHelper.sendTextByKey(source, "jail.put.failed", playerName);
                    JailService.disableActiveJailRecord(playerName);
                }
                return CommandHelper.Return.SUCCESS;
            });
    }

    @Document(id = 1753690598413L, value = """
        Remove a player from the jail it is currently in.
        """)
    @CommandNode("jail pardon")
    @CommandRequirement(level = 4)
    private static int $pardon(@CommandSource ServerCommandSource source, JailedPlayerName playerName) {
        String $playerName = playerName.getValue();

        return JailService
            .getCurrentJailRecord($playerName)
            .map(currentJailRecord -> {
                JailService.pardonJailRecord(currentJailRecord);
                TextHelper.sendTextByKey(source, "jail.pardon", $playerName, currentJailRecord.getOwnerJailDescriptor().getId());
                return CommandHelper.Return.SUCCESS;
            })
            .orElseGet(() -> {
                TextHelper.sendTextByKey(source, "jail.not_in_jail", $playerName);
                return CommandHelper.Return.FAIL;
            });
    }

    @Document(id = 1753692574518L, value = "Find the `jail` the player is in.")
    @CommandNode("jail which-jail")
    @CommandRequirement(level = 4)
    private static int $whichJail(@CommandSource ServerCommandSource source, JailedPlayerName playerName) {
        String $playerName = playerName.getValue();
        return JailService
            .getCurrentJailRecord($playerName)
            .map(jailRecord -> {
                JailDescriptor ownerJailDescriptor = jailRecord.getOwnerJailDescriptor();
                TextHelper.sendTextByKey(source, "line.separator");
                TextHelper.sendTextByKey(source, "jail.record.player_name", $playerName);
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
                return CommandHelper.Return.FAIL;
            });
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
    protected void registerPlaceholder() {
        JailPlaceholders.registerCurrentJailId();
        JailPlaceholders.registerCurrentJailDisplayName();
        JailPlaceholders.registerCurrentJailCreatorName();
        JailPlaceholders.registerCurrentJailCreatedDate();
        JailPlaceholders.registerCurrentJailSpecifiedJailDuration();
        JailPlaceholders.registerCurrentJailRemainingJailDuration();
        JailPlaceholders.registerCurrentJailReason();

    }
}

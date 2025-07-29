package io.github.sakurawald.fuji.module.initializer.jail;

import io.github.sakurawald.fuji.core.auxiliary.ChronosUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.GreedyString;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.OfflinePlayerName;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.event.impl.ServerLifecycleEvents;
import io.github.sakurawald.fuji.core.manager.Managers;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.jail.command.argument.wrapper.JailedPlayerName;
import io.github.sakurawald.fuji.module.initializer.jail.config.model.JailConfigModel;
import io.github.sakurawald.fuji.module.initializer.jail.config.model.JailDataModel;
import io.github.sakurawald.fuji.module.initializer.jail.job.UpdateJailRecordsJob;
import io.github.sakurawald.fuji.module.initializer.jail.service.JailService;
import io.github.sakurawald.fuji.module.initializer.jail.structure.JailDescriptor;
import java.util.Optional;
import net.minecraft.server.command.ServerCommandSource;

@Document(id = 1753681022357L, value = """
    This module allows you to define a `jail`.
    It can be used to `punish` a player with bad behaviour, without `banning` it.
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
                JailService.createJailRecord(creatorName, playerName, jail, $reason, $duration);
                TextHelper.sendTextByKey(source, "jail.put", playerName, jail.getId());
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
                JailService.pardonJailRecord(currentJailRecord, $playerName);
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
                TextHelper.sendTextByKey(source,"jail.record.created_time", ChronosUtil.toDefaultDateFormat(jailRecord.getCreatedTimestamp()));
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
    }
}

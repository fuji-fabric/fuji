package io.github.sakurawald.fuji.module.initializer.gameplay.carpet.fake_player_manager;

import io.github.sakurawald.fuji.core.annotation.Unused;
import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.event.annotation.EventConsumer;
import io.github.sakurawald.fuji.core.event.message.server.lifecycle.ServerStartedEvent;
import io.github.sakurawald.fuji.core.manager.Managers;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.gameplay.carpet.fake_player_manager.config.model.FakePlayerManagerConfigModel;
import io.github.sakurawald.fuji.module.initializer.gameplay.carpet.fake_player_manager.job.ManageFakePlayersJob;
import io.github.sakurawald.fuji.module.initializer.gameplay.carpet.fake_player_manager.service.FakePlayerManagerService;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

@Document(id = 1751827019419L, value = """
    This module provides `fake player management` for `carpet` mod.
    """)
@ColorBox(id = 1753153100239L, color = ColorBox.ColorBoxTypes.NOTE, value = """
    ◉ This module introduces the `authority` for each `fake-player`.
    1. Disables the `/player shadow` command.
    2. For each `fake-player`, the `player` who `spawned` it is its `owner player`.
    3. For each `fake-player`, it can only be `interacted` (right-click) with by its `owner player`.
    4. For each `fake-player`, it can only be `manipulated` (The `/player` command) by its `owner player`.
    5. The `console`, `the ops` or `the owner player` are considered authorized.

    ◉ This module allows you to define the naming-format for `fake-player`.
    You can define a `prefix` and `suffix` for fake player names.
    The `fake player name` argument value will be transformed when you use `/player <playerName> spawn` command.

    ◉ This module introduce the `spawn caps limit` and `expiration time` for each fake player.
    1. You can define the `spawn caps limit` at different time.
    2. You can specify the `max living duration` for each fake player. (By default, it is `12h`.)
    3. A player can use `/player renew` command to `renew` the `expiration time` for all of its fake players.
    4. The `console` can bypass the `spawn caps limit`.
    """)
public class FakePlayerManagerInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<FakePlayerManagerConfigModel> config = ObjectConfigurationHandler.ofModule(BaseConfigurationHandler.CONFIG_JSON_LITERAL, FakePlayerManagerConfigModel.class);

    @Document(id = 1751827022331L, value = "Renew the expiration time of `all` fake-player you have `spawned`.")
    @CommandNode("player renew")
    private static int $renew(@CommandSource ServerPlayerEntity player) {
        FakePlayerManagerService.renewMyFakePlayers(player);
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751827025708L, value = "List all fake-player and its owner player.")
    @CommandNode("player who")
    private static int $who(@CommandSource ServerCommandSource source) {
        TextHelper.sendTextByKey(source, "fake_player_manager.who.header");
        TextHelper.sendMessageByText(source, TextHelper.Formatter.formatMapMultiLine(FakePlayerManagerService.player2fakePlayers));
        return CommandHelper.Return.SUCCESS;
    }

    @EventConsumer
    private static void scheduleFakePlayerJob(@Unused ServerStartedEvent event) {
        ManageFakePlayersJob manageFakePlayersJob = new ManageFakePlayersJob();
        Managers.getScheduleManager().scheduleJob(manageFakePlayersJob);
    }

}

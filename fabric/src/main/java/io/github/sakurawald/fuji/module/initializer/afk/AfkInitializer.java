package io.github.sakurawald.fuji.module.initializer.afk;

import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.command.annotation.CommandTarget;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.extension.PlayerCombatExtension;
import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.afk.accessor.AfkStateAccessor;
import io.github.sakurawald.fuji.module.initializer.afk.config.model.AfkConfigModel;
import io.github.sakurawald.fuji.module.initializer.afk.service.AfkService;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;


@Document(id = 1751826238005L, value = """
    This module provides:
    1. Afk detection: If a player idle too long, we will mark it as afk state.
    2. Afk event: Execute commands when a player enters or leaves afk state.
    3. Afk name customization: For a afk player, we can customize its display name in tab list.
    """)
@ColorBox(id = 1751870451351L, color = ColorBox.ColorBoxTypes.NOTE, value = """
    ◉ How it works?

    For each player, define a `number` to track `the last action time`.
    Actions can be: `mine a block`, `movement`, `issue a command` ...
    When action received, update the number.
    Define a `job` using cron, to be triggered periodically.
    The job will check and compare 2 consecutive value of the `number`.
    If number is identical, then the player is considered as in `afk`.
    """)
public class AfkInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<AfkConfigModel> config = ObjectConfigurationHandler.ofModule(BaseConfigurationHandler.CONFIG_JSON_LITERAL, AfkConfigModel.class);

    @CommandNode("afk")
    @Document(id = 1751826266551L, value = "Enter afk state.")
    private static int $afk(@CommandSource @CommandTarget ServerPlayerEntity player) {
        // NOTE: Issue a command will update the lastLastActionTime, so it's impossible to use /afk to disable afk
        if (!player.isOnGround()
            || player.isOnFire()
            || player.inPowderSnow
            || ((PlayerCombatExtension) player).fuji$inCombat()) {

            TextHelper.sendTextByKey(player, "afk.on.failed");
            return CommandHelper.Return.FAILURE;
        }

        ((AfkStateAccessor) player).fuji$changeAfk(true);
        TextHelper.sendTextByKey(player, "afk.on");
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751826271499L, value = "Test if a player is in afk state.")
    @CommandNode("test-afk")
    @CommandRequirement(level = 4)
    private static int $testAfk(@CommandSource ServerCommandSource source, ServerPlayerEntity player) {
        boolean value = AfkService.isAfk(player);
        return CommandHelper.Return.returnBoolean(source, value);
    }

}

package mod.fuji.module.initializer.afk;

import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandRequirement;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.core.command.annotation.CommandTarget;
import mod.fuji.core.config.handler.abst.BaseConfigurationHandler;
import mod.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import mod.fuji.core.document.annotation.ColorBox;
import mod.fuji.module.initializer.ModuleInitializer;
import mod.fuji.module.initializer.afk.config.model.AfkConfigModel;
import mod.fuji.module.initializer.afk.service.AfkService;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;


@Document(id = 1751826238005L, value = """
    This module adds a `checker` to check if a player is in `Away From Keyboard` state.
    With the `afk` state:
    1. You can define `commands` to be executed when a player `enters` or `leaves` this state.
    2. You can define the `display name` in this state.
    """)
@ColorBox(id = 1751870451351L, color = ColorBox.ColorBoxTypes.NOTE, value = """
    ◉ How it works?

    For each player, define a `number` to track `the last action time`.
    The `actions` can be: `mine a block`, `movement`, `issue a command`...
    When an `action` received, update the number.

    Define a `job` to compare 2 consecutive values of the `number`.
    If values are identical, then the player is considered as in `afk` state.
    """)
public class AfkInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<AfkConfigModel> config = ObjectConfigurationHandler.ofModule(BaseConfigurationHandler.CONFIG_JSON_LITERAL, AfkConfigModel.class);

    @CommandNode("afk")
    @Document(id = 1751826266551L, value = "Enter afk state.")
    private static int $afk(@CommandSource @CommandTarget ServerPlayer player) {
        if (!AfkService.canEnterAfkState(player)) {
            TextHelper.sendTextByKey(player, "afk.on.failed");
            return CommandHelper.Return.FAILURE;
        }

        // NOTE: Issue a command will update the lastLastActionTime, so it's impossible to use /afk to disable afk
        AfkService.changeAfkState(player, true);
        TextHelper.sendTextByKey(player, "afk.on");
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751826271499L, value = "Test if a player is in afk state.")
    @CommandNode("is-afk?")
    @CommandRequirement(level = 4)
    private static int $isAfk(@CommandSource CommandSourceStack source, ServerPlayer player) {
        boolean value = AfkService.isInAfkState(player);
        return CommandHelper.Return.returnBoolean(source, value);
    }

}

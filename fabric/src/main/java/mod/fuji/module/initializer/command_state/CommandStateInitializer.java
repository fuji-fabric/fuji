package mod.fuji.module.initializer.command_state;

import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandRequirement;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.core.config.handler.abst.BaseConfigurationHandler;
import mod.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import mod.fuji.core.document.annotation.ColorBox;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.manager.Managers;
import mod.fuji.module.initializer.ModuleInitializer;
import mod.fuji.module.initializer.command_state.config.model.CommandStateConfigModel;
import mod.fuji.module.initializer.command_state.config.model.CommandStateDataModel;
import mod.fuji.module.initializer.command_state.gui.ListPlayerStatesGui;
import mod.fuji.module.initializer.command_state.job.CommandStateAutoUpdaterJob;
import mod.fuji.module.initializer.command_state.service.CommandStateService;
import mod.fuji.module.initializer.command_state.structure.StateDescriptor;
import java.util.List;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

@Document(id = 1756692824395L, value = """
    This module allows defining a `state` using `predicate commands`.
    Using this defined `state`, you can:
    - Define commands to be executed when a player `enters` or `leaves` this `state`.
    - Check whether a player is currently in the `state`.
    - Display `placeholders` relevant to the `state`.
    """)
@ColorBox(id = 1756707949343L, color = ColorBox.ColorBoxTypes.TIP, value = """
    ◉ Define a `state` using `predicate commands`.
    A `state` is composed by `predicate commands`.

    You can define a `state` called `has-iron-and-gold`, with the following `predicate commands`:
    1. `has-item? %player:name% minecraft:iron_ingot 16`
    2. `has-item? %player:name% minecraft:gold_ingot 8`

    ◉ Integrate with `luckperms`'s `temporary permission`.
    Assign a `temporary permission` using `/lp group default permission settemp fuji.permission.fly true 10s`

    Then define a `state` to check whether a player `has specified temporary permission`.
    This `state` can be called `can-use-fly-command` with the following `predicate commands`:
    1. `has-perm? %player:name% fuji.permission.fly`

    Last, you can define `commands to be executed` when a player `leave this state`.
    For example, define commands to `turn off the flying` while the player `leaves` the `state`.

    ◉ Check the value of a `state` of a player.
    Issue: `/command-state info Steve`

    Besides that, the `/is-in-state? \\<player\\> \\<state-id\\>` can be used as a `predicate command`.
    """)


@CommandRequirement(level = 4)
public class CommandStateInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<CommandStateConfigModel> config = ObjectConfigurationHandler
        .ofModule(BaseConfigurationHandler.CONFIG_JSON_LITERAL, CommandStateConfigModel.class);

    public static final BaseConfigurationHandler<CommandStateDataModel> data = ObjectConfigurationHandler
        .ofModule("command-state-data.json", CommandStateDataModel.class)
        .enableAutoSaveFeature();

    @CommandNode("command-state list")
    @Document(id = 1756695958335L, value = "List all defined `states`.")
    private static int $list(@CommandSource ServerCommandSource source) {
        List<String> ids = CommandStateService.listStateIds();
        TextHelper.sendTextByKey(source, "command_state.list", ids);
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("command-state update")
    @Document(id = 1756695727818L, value = "Update the specified `state` for online players.")
    private static int $update(@CommandSource ServerCommandSource source, StateDescriptor state) {
        CommandStateService.updateCommandState(state);
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("command-state update-all")
    @Document(id = 1756695744117L, value = "Update all defined `states` for online players.")
    private static int $updateAll(@CommandSource ServerCommandSource source) {
        CommandStateService.updateAllCommandStates();
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("command-state info")
    @Document(id = 1756695758012L, value = "Display the value of all `states` of the specified player.")
    private static int $info(@CommandSource ServerPlayerEntity source, ServerPlayerEntity player) {
        ListPlayerStatesGui
            .make(source, player)
            .open();
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("is-in-state?")
    @Document(id = 1756695870856L, value = "Returns whether the specified `state` value for the player is currently `true`.")
    private static int $isInState(@CommandSource ServerCommandSource source, ServerPlayerEntity player, StateDescriptor state) {
        boolean inState = CommandStateService.checkCurrentStateValue(player, state);
        return CommandHelper.Return.returnBoolean(source, inState);
    }

    @Override
    protected void onInitialize() {
        Managers.getScheduleManager().addJob(new CommandStateAutoUpdaterJob());
    }

    @Override
    protected void registerPlaceholders() {
        CommandStatePlaceholders.registerIsInStatePlaceholder();
    }
}

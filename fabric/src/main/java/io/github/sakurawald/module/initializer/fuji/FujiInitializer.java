package io.github.sakurawald.module.initializer.fuji;

import io.github.sakurawald.core.annotation.Document;
import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.command.annotation.CommandNode;
import io.github.sakurawald.core.command.annotation.CommandRequirement;
import io.github.sakurawald.core.command.annotation.CommandSource;
import io.github.sakurawald.core.config.Configs;
import io.github.sakurawald.core.gui.inspection.CommandsInspectionGui;
import io.github.sakurawald.core.job.abst.BaseJob;
import io.github.sakurawald.core.manager.Managers;
import io.github.sakurawald.core.manager.impl.module.ModuleManager;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import io.github.sakurawald.module.initializer.fuji.gui.AboutGui;
import io.github.sakurawald.module.initializer.fuji.gui.ArgumentTypesInspectionGui;
import io.github.sakurawald.module.initializer.fuji.gui.ConfigurationsInspectionGui;
import io.github.sakurawald.module.initializer.fuji.gui.ModulesInspectionGui;
import io.github.sakurawald.module.initializer.fuji.gui.PermissionsAndMetasInspectionGui;
import io.github.sakurawald.module.initializer.fuji.gui.PlaceholdersInspectionGui;
import io.github.sakurawald.module.initializer.fuji.gui.RegistriesInspectionGui;
import io.github.sakurawald.module.initializer.fuji.gui.ServerCommandsInspectionGui;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;


@Document("""
    Provides `/fuji` command.
    To reload the configs of fuji.
    To inspect states of fuji.
    To discover things of fuji.
    """)
@CommandNode("fuji")
@CommandRequirement(level = 4)
public class FujiInitializer extends ModuleInitializer {

    @Document("""
        Reload all the configuration files in `/fuji inspect configurations`.
        Reload all the `enabled` modules in `/fuji inspect modules`.

        NOTE: You have to `re-start` the server, after you enable/disable a module.
        """)
    @CommandNode("reload")
    private static int $reload(@CommandSource ServerCommandSource source) {
        // Reload main-control file.
        Configs.mainControlConfig.readStorage();

        // Reload modules.
        Managers.getModuleManager().reloadModuleInitializers();

        // Reload jobs.
        BaseJob.rescheduleAll();

        TextHelper.sendMessageByKey(source, "reload");
        return CommandHelper.Return.SUCCESS;
    }

    @Document("Print the user guide of fuji.")
    @CommandNode("user-guide")
    private static int $userGuide(@CommandSource ServerCommandSource source) {
        ModuleManager.printUserGuide();
        TextHelper.sendMessageByKey(source, "fuji.user_guide");
        return CommandHelper.Return.SUCCESS;
    }

    @Document("Open the about GUI.")
    @CommandNode("about")
    private static int $about(@CommandSource ServerPlayerEntity player) {
        AboutGui
            .make(player)
            .open();
        return CommandHelper.Return.SUCCESS;
    }

    @Document("Toggle the debug mode of fuji.")
    @CommandNode("debug")
    private static int $debug(@CommandSource ServerCommandSource source) {
        var config = Configs.mainControlConfig.model().core.debug;
        config.log_debug_messages = !config.log_debug_messages;

        TextHelper.sendMessageByKey(source, config.log_debug_messages ? "fuji.debug.on" : "fuji.debug.off");
        return CommandHelper.Return.SUCCESS;
    }

    @Document("Inspect all commands registered in server.")
    @CommandNode("inspect server-commands")
    private static int $inspectServerCommands(@CommandSource ServerPlayerEntity player) {
        ServerCommandsInspectionGui
            .inspectAll(player)
            .open();
        return CommandHelper.Return.SUCCESS;
    }

    @Document("Inspect all enabled/disabled modules of fuji.")
    @CommandNode("inspect modules")
    private static int $inspectModules(@CommandSource ServerPlayerEntity player) {
        ModulesInspectionGui
            .inspectAll(null, player)
            .open();
        return CommandHelper.Return.SUCCESS;
    }

    @Document("Inspect all commands registered by fuji.")
    @CommandNode("inspect fuji-commands")
    private static int $inspectFujiCommands(@CommandSource ServerPlayerEntity player) {
        CommandsInspectionGui
            .inspectAll(null, player)
            .open();
        return CommandHelper.Return.SUCCESS;
    }

    @Document("Inspect all argument types registered by fuji.")
    @CommandNode("inspect argument-types")
    private static int $inspectCommandArgumentTypes(@CommandSource ServerPlayerEntity player) {
        ArgumentTypesInspectionGui
            .inspectAll(null, player)
            .open();
        return CommandHelper.Return.SUCCESS;
    }

    @Document("Inspect all loaded configurations files used by fuji.")
    @CommandNode("inspect configurations")
    private static int $inspectConfigurations(@CommandSource ServerPlayerEntity player) {
        ConfigurationsInspectionGui
            .inspectAll(null, player)
            .open();
        return CommandHelper.Return.SUCCESS;
    }

    @Document("Inspect all registries in server.")
    @CommandNode("inspect registry")
    private static int $inspectRegistry(@CommandSource ServerPlayerEntity player) {
        RegistriesInspectionGui
            .inspectAll(player)
            .open();
        return CommandHelper.Return.SUCCESS;
    }

    @Document("Inspect permissions and metas used by fuji.")
    @CommandNode("inspect permissions-and-metas")
    private static int $inspectPermissionsAndMetas(@CommandSource ServerPlayerEntity player) {
        PermissionsAndMetasInspectionGui
            .inspectAll(null, player)
            .open();
        return CommandHelper.Return.SUCCESS;
    }

    @Document("Inspect placeholders registered by fuji.")
    @CommandNode("inspect placeholders")
    private static int $inspectPlaceholders(@CommandSource ServerPlayerEntity player) {
        PlaceholdersInspectionGui
            .inspectAll(null, player)
            .open();
        return CommandHelper.Return.SUCCESS;
    }

}


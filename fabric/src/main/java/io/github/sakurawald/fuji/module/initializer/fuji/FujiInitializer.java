package io.github.sakurawald.fuji.module.initializer.fuji;

import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.config.Configs;
import io.github.sakurawald.fuji.core.document.gui.CommandsInspectionGui;
import io.github.sakurawald.fuji.core.manager.Managers;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.core.CoreInitializer;
import io.github.sakurawald.fuji.module.initializer.fuji.gui.AboutGui;
import io.github.sakurawald.fuji.module.initializer.fuji.gui.ArgumentTypesInspectionGui;
import io.github.sakurawald.fuji.module.initializer.fuji.gui.ConfigurationsInspectionGui;
import io.github.sakurawald.fuji.module.initializer.fuji.gui.JobsInspectionGui;
import io.github.sakurawald.fuji.module.initializer.fuji.gui.LanguagesInspectionGui;
import io.github.sakurawald.fuji.module.initializer.fuji.gui.MixinsInspectionGui;
import io.github.sakurawald.fuji.module.initializer.fuji.gui.ModulesInspectionGui;
import io.github.sakurawald.fuji.module.initializer.fuji.gui.PermissionsAndMetasInspectionGui;
import io.github.sakurawald.fuji.module.initializer.fuji.gui.PlaceholdersInspectionGui;
import io.github.sakurawald.fuji.module.initializer.fuji.gui.RegistriesInspectionGui;
import io.github.sakurawald.fuji.module.initializer.fuji.gui.ServerCommandsInspectionGui;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;


@Document(id = 1751826545831L, value = """
    Provides `/fuji` command.
    To reload the configs of fuji.
    To inspect states of fuji.
    To discover things of fuji.
    """)
@CommandNode("fuji")
@CommandRequirement(level = 4)
public class FujiInitializer extends ModuleInitializer {

    @Document(id = 1751826549358L, value = """
        Reload all the configuration files in `/fuji inspect configurations`.
        Reload all the `enabled` modules in `/fuji inspect modules`.

        NOTE: You have to `re-start` the server, after you enable/disable a module.
        """)
    @CommandNode("reload")
    public static int $reload(@CommandSource ServerCommandSource source) {
        // Reload main-control file.
        Configs.MAIN_CONTROL_CONFIG.readStorage();

        // Reload the language files.
        TextHelper.Loader.clearLoadedLanguageJsons();

        // Reload modules.
        Managers.getModuleManager().reloadModuleInitializers();

        // Reload jobs.
        Managers.getScheduleManager().rescheduleJobs();

        TextHelper.sendTextByKey(source, "reload");
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751826551723L, value = "Print the user guide of fuji.")
    @CommandNode("user-guide")
    public static int $userGuide(@CommandSource ServerCommandSource source) {
        CoreInitializer.printUserGuide();
        TextHelper.sendTextByKey(source, "fuji.user_guide");
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751826554192L, value = "Open the about GUI.")
    @CommandNode("about")
    private static int $about(@CommandSource ServerPlayerEntity player) {
        AboutGui
            .make(null, player)
            .open();
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751826556095L, value = "Toggle the debug mode of fuji.")
    @CommandNode("debug")
    public static int $debug(@CommandSource ServerCommandSource source) {
        var config = Configs.MAIN_CONTROL_CONFIG.model().core.debug;
        config.log_debug_messages = !config.log_debug_messages;

        TextHelper.sendTextByKey(source, config.log_debug_messages ? "fuji.debug.on" : "fuji.debug.off");
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751826560904L, value = "Inspect all commands registered in server.")
    @CommandNode("inspect server-commands")
    private static int $inspectServerCommands(@CommandSource ServerPlayerEntity player) {
        ServerCommandsInspectionGui
            .inspectAll(player)
            .open();
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751826563793L, value = "Inspect all enabled/disabled modules of fuji.")
    @CommandNode("inspect modules")
    private static int $inspectModules(@CommandSource ServerPlayerEntity player) {
        ModulesInspectionGui
            .inspectAll(null, player)
            .open();
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751826565988L, value = "Alias to `/fuji inspect modules`.")
    @CommandNode
    private static int $inspectModulesShortcut(@CommandSource ServerPlayerEntity player) {
        $inspectModules(player);
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751826568226L, value = "Alias to `/fuji inspect modules`.")
    @CommandNode("gui")
    private static int $gui(@CommandSource ServerPlayerEntity player) {
        $inspectModules(player);
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751826570905L, value = "Inspect all commands registered by fuji.")
    @CommandNode("inspect fuji-commands")
    private static int $inspectFujiCommands(@CommandSource ServerPlayerEntity player) {
        CommandsInspectionGui
            .inspectAll(null, player)
            .open();
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751826572555L, value = "Inspect all argument types registered by fuji.")
    @CommandNode("inspect argument-types")
    private static int $inspectCommandArgumentTypes(@CommandSource ServerPlayerEntity player) {
        ArgumentTypesInspectionGui
            .inspectAll(null, player)
            .open();
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751826574793L, value = "Inspect all loaded configurations files used by fuji.")
    @CommandNode("inspect configurations")
    private static int $inspectConfigurations(@CommandSource ServerPlayerEntity player) {
        ConfigurationsInspectionGui
            .inspectAll(null, player)
            .open();
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751826576778L, value = "Inspect all registries in server.")
    @CommandNode("inspect registry")
    private static int $inspectRegistry(@CommandSource ServerPlayerEntity player) {
        RegistriesInspectionGui
            .inspectAll(player)
            .open();
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751826579437L, value = "Inspect permissions and metas used by fuji.")
    @CommandNode("inspect permissions-and-metas")
    private static int $inspectPermissionsAndMetas(@CommandSource ServerPlayerEntity player) {
        PermissionsAndMetasInspectionGui
            .inspectAll(null, player)
            .open();
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751826581359L, value = "Inspect placeholders registered by fuji.")
    @CommandNode("inspect placeholders")
    private static int $inspectPlaceholders(@CommandSource ServerPlayerEntity player) {
        PlaceholdersInspectionGui
            .inspectAll(null, player)
            .open();
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751826583345L, value = "Inspect jobs registered by fuji.")
    @CommandNode("inspect jobs")
    private static int $inspectJobs(@CommandSource ServerPlayerEntity player) {
        JobsInspectionGui
            .inspectAll(null, player)
            .open();
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1752003106392L, value = "Inspect loaded language files by fuji.")
    @CommandNode("inspect languages")
    private static int $inspectLanguages(@CommandSource ServerPlayerEntity player) {
        LanguagesInspectionGui
            .inspectAll(null, player)
            .open();
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1756906211441L, value = "Inspect applied mixins by fuji.")
    @CommandNode("inspect mixins")
    private static int $inspectMixins(@CommandSource ServerPlayerEntity player) {
        MixinsInspectionGui
            .inspectAll(player)
            .open();
        return CommandHelper.Return.SUCCESS;
    }
}


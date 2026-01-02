package mod.fuji.module.initializer.core;

import java.util.List;
import mod.fuji.core.annotation.Unused;
import mod.fuji.core.auxiliary.LogUtil;
import mod.fuji.core.auxiliary.minecraft.ServerHelper;
import mod.fuji.core.config.Configs;
import mod.fuji.core.document.annotation.ColorBox;
import mod.fuji.core.event.annotation.EventConsumer;
import mod.fuji.core.event.message.server.lifecycle.ServerStartedEvent;
import mod.fuji.core.module.ModuleLoadDeterminer;
import mod.fuji.module.initializer.ModuleInitializer;
import org.jetbrains.annotations.NotNull;

@ColorBox(id = 1751870436910L, color = ColorBox.ColorBoxTypes.TIP, value = """
    ◉ How to use this mod?
    Functions are provided by modules.
    All modules are disabled by default.
    You need to enable the modules you want to use.

    ◉ How to enable a module?
    1. Edit the `<your-server>/config/fuji/config.json` file.
    2. In `modules` section, set the `true` and `false` values for each module.
    3. Re-start the server to apply the changes.
    """)
@ColorBox(id = 1751870440489L, color = ColorBox.ColorBoxTypes.TIP, value = """
    ◉ Can I reload the config files while the server is running?
    Yes.
    You can do that only for a `enabled` module.
    For such module, use `/fuji reload` to reload the module specific files.

    You can not `enable` or `disable` a module while the server is running.
    Modules are only loaded during the server start-up.

    It's a design choice, for reasons:
    1. For performance: You never pay for the disabled modules.
    2. For compatibility: If conflicting with other mods, you can disable the related module to solve it.
    """)
@ColorBox(id = 1752891903903L, color = ColorBox.ColorBoxTypes.TIP, value = """
    ◉ How can I make the lore text easier to read?

    ➜ Adjust lore text font size if too large
    If lore text is too large:
    - Open `Esc` → `Options` → `Video Settings` → `GUI Scale`.

    ➜ Improve lore text rendering if necessary
    If `GUI scaling` isn't enough, install the client-side mod `ToolTipFix`:
    - https://modrinth.com/mod/tooltipfix
    - https://www.curseforge.com/minecraft/mc-mods/tooltipfix
    """)
@ColorBox(id = 1753331128791L, color = ColorBox.ColorBoxTypes.TIP, value = """
    ◉ How can I make the config file easier to edit?
    Please use a modern text editor.

    Most configuration files are written in `JSON format` and may be large.
    A modern `text editor` provides `structure highlighting` and `error checking` features.
    It makes configuration easier.

    Recommended text editors:
    1. Visual Studio Code: https://code.visualstudio.com/
    2. Vim: https://neovim.io/
    3. Emacs: https://www.gnu.org/software/emacs/
    """)
@ColorBox(id = 1754014854649L, color = ColorBox.ColorBoxTypes.TIP, value = """
    ◉ How can I know what is provided by this mod?

    ➜ Open the interactive document GUI
    Issue: `/fuji`

    ➜ List all commands
    Issue: `/fuji inspect fuji-commands`

    ➜ List all permissions and metas
    Issue: `/fuji inspect permissions-and-metas`

    ➜ List all placeholders
    Issue: `/fuji inspect placeholders`

    ➜ List all configurations
    Issue: `/fuji inspect configurations`

    ➜ List all jobs
    Issue: `/fuji inspect jobs`

    ➜ List all languages
    Issue: `/fuji inspect languages`

    ➜ List all argument types
    Issue: `/fuji inspect argument-types`

    ➜ List all events
    Issue: `/fuji inspect events`
    """)
public class CoreInitializer extends ModuleInitializer {

    public static void printUserGuide() {
        // NOTE: The generator is https://rebane2001.com/discord-colored-text-generator/
        String userGuide = """
             [2;35m[1;35m
             [Fuji User Guide][0m[2;35m
             This is the user guide for new users.
             To disable this user guide, you can set `print_user_guide_in_console` in `config/fuji/config.json` to `false`.

             Here are some important points:
             - Fuji is designed to be fully-modular. [2;34mAll modules are disabled by default.[0m[2;35m
             - To enable a module: modify the `[2;34mconfig/fuji/config.json[0m[2;35m` file, and [2;34mre-start[0m[2;35m the server to apply the modification.
                 - To use `/tpa` command, enable the `tpa` module.
                 - To use placeholders provided by fuji, enable the `placeholder` module.
                 - To use echo commands like `/send-message`, `/send-broadcast` etc, enable the `echo` module.
             - To see the overview document, read the `fuji manual` pdf file in [2;34mhttps://github.com/fuji-fabric/fuji/raw/dev/docs/release/fuji.pdf[0m[2;35m
             - To discover new things, use `/fuji inspect` command.
             - Anything unclear, open an issue in [2;34mhttps://github.com/fuji-fabric/fuji/issues[0m[2;35m[0m[2;35m
             - Now, issue `[2;34m/fuji[0m[2;35m` to get started!

             \033[0m
            """;
        LogUtil.info(userGuide);
    }

    private static void tryPrintUserGuide() {
        if (Configs.MAIN_CONTROL_CONFIG.model().core.debug.print_user_guide_in_console) {
            printUserGuide();
        }
    }

    @EventConsumer(injectorPriority = EventConsumer.HIGHEST, consumerPriority = EventConsumer.HIGHEST)
    private static void printModuleStatusReportOnServerStarted(@Unused ServerStartedEvent event) {
        /* Report enabled/disabled modules. */
        List<String> enabledModuleList = ModuleLoadDeterminer.getEnabledModulePaths();
        LogUtil.info("Enabled {}/{} modules -> {}", enabledModuleList.size(), ModuleLoadDeterminer.MODULE_ENABLE_STATUS.size(), enabledModuleList);

        /* Print the user guide for new users. */
        tryPrintUserGuide();
    }

    public static @NotNull String getModVersion() {
        return ServerHelper.ModInfo.getSelfModContainer().getMetadata().getVersion().getFriendlyString();
    }

}

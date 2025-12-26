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
    All modules are disabled by default.
    Enable only the modules you need.

    Steps:
    1. Edit `config/fuji/config.json` file.
    2. Set the desired modules to `enabled`.
    3. Re-start the server to apply the changes.
    """)

@ColorBox(id = 1751870440489L, color = ColorBox.ColorBoxTypes.NOTE, value = """
    ◉ Does this mod support hot reload?
    Yes. It supports hot reloading for files belonging to `enabled` modules.
    Use the command `/fuji reload` to do that.

    Limitations:
    - Modules cannot be `enabled` or `disabled` while the server is running.

    Design rationale:
    1. Disabled modules are never loaded.
    2. You can freely disable unwanted modules.
    3. Module-level conflicts can be resolved by disabling only the affected module.
    4. Disabled modules incur no performance cost.
    """)

@ColorBox(id = 1752891903903L, color = ColorBox.ColorBoxTypes.TIP, value = """
    ◉ Adjust lore text font size
    If lore text appears too large:
    - Open `Esc` → `Options` → `Video Settings` → `GUI Scale`.

    ◉ Improve tooltip rendering
    If `GUI scaling` is insufficient, install the client-side mod `ToolTipFix`:
    - https://modrinth.com/mod/tooltipfix
    - https://www.curseforge.com/minecraft/mc-mods/tooltipfix
    """)

@ColorBox(id = 1753331128791L, color = ColorBox.ColorBoxTypes.TIP, value = """
    ◉ Use a modern text editor
    Most configuration files are written in JSON and may be large.
    A modern text editor provides `syntax highlighting` and `error checking`.
    It makes configuration easier and safer.

    Recommended text editors:
    1. Visual Studio Code: https://code.visualstudio.com/
    2. Vim: https://neovim.io/
    3. Emacs: https://www.gnu.org/software/emacs/
    """)

@ColorBox(id = 1753331405512L, color = ColorBox.ColorBoxTypes.TIP, value = """
    ◉ Set up a local test server
    If you run a remote production server, it is strongly recommended to maintain a local test server.
    The test server should mirror the production environment:
    1. Same mods
    2. Same configuration files

    Workflow:
    1. Test changes locally.
    2. Verify everything works as expected.
    3. Upload the updated mods and configurations to the production server.
    """)

@ColorBox(id = 1754014854649L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ Open the interactive document GUI
    Issue: `/fuji`

    ◉ List all Fuji commands
    Issue: `/fuji inspect fuji-commands`

    ◉ List all Fuji permissions and metas
    Issue: `/fuji inspect permissions-and-metas`

    ◉ List all Fuji placeholders
    Issue: `/fuji inspect placeholders`

    ◉ List all Fuji configurations
    Issue: `/fuji inspect configurations`

    ◉ List all Fuji jobs
    Issue: `/fuji inspect jobs`

    ◉ List all Fuji languages
    Issue: `/fuji inspect languages`

    ◉ List all Fuji argument types
    Issue: `/fuji inspect argument-types`

    ◉ List all Fuji events
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

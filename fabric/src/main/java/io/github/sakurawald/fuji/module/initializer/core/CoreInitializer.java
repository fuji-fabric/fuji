package io.github.sakurawald.fuji.module.initializer.core;

import io.github.sakurawald.fuji.core.annotation.Unused;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.fuji.core.config.Configs;
import io.github.sakurawald.fuji.core.event.annotation.EventConsumer;
import io.github.sakurawald.fuji.core.event.message.impl.on_demand.server.lifecycle.ServerStartedEvent;
import io.github.sakurawald.fuji.core.manager.impl.module.ModuleLoadDeterminer;
import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;

import java.util.List;
import org.jetbrains.annotations.NotNull;

@ColorBox(id = 1751870436910L, color = ColorBox.ColorBoxTypes.TIPS, value = """
    ◉ How to use fuji?

    All `modules` are `disabled` by default.
    You can only enable the `interested modules`.
    Modify the `config/fuji/config.json` file to `enable` a module.
    After that, `re-start` the server, to apply the `module enable status`.
    """)
@ColorBox(id = 1751870440489L, color = ColorBox.ColorBoxTypes.NOTE, value = """
    ◉ Does fuji support the `hot reload`?

    Yes, fuji does support to `hot reload` the `files` from a `enabled module`.
    To do that, just issue `/fuji reload`.

    However, you can't `enable` or `disable` a module when the server `is running`.
    This is a design decision.
    Fuji will `never load` a `disabled module` at all, for these considerations:
    1. For flexible, you can `disable any module` you don't like.
    2. If any other mods conflicts with `a module`, you can just `disable that module`.
    3. You only enable the `interested modules`, and there is no performance paying for `disabled module`.
    """)
@ColorBox(id = 1752891903903L, color = ColorBox.ColorBoxTypes.TIPS, value = """
    ◉ Adjust the `lore` text font size.
    If the `lore` text is too large in your UI.
    You can configure it in `Esc` - `Options` - `Video Settings` - `GUI Scale`

    ◉ Install the `client-side` mod to improve the displaying of `tooltip`.
    If adjusting the `GUI Scale` option doesn't work well for you.
    You can install the `ToolTipFix` mod to enhance the `tooltip` displaying.
    - https://modrinth.com/mod/tooltipfix
    - https://www.curseforge.com/minecraft/mc-mods/tooltipfix
    """)
@ColorBox(id = 1753331128791L, color = ColorBox.ColorBoxTypes.TIPS, value = """
    ◉ Use a `modern` text editor.
    The most of `config files` are written in `json language`, and contains lots of lines.
    A `modern` text editor can `highlight` the structure of the file, and check the `syntax errors` for you.
    So that you can `read` and `edit` the config files easier.

    Here are recommended text editors:
    1. Visual Studio Code: https://code.visualstudio.com/
    2. Vim: https://neovim.io/
    3. Emacs: https://www.gnu.org/software/emacs/
    """)
@ColorBox(id = 1753331405512L, color = ColorBox.ColorBoxTypes.TIPS, value = """
    ◉ Setup a `test server` in your `local machine`.
    You may have a `remote machine` (Typically named `production server`) that is `running` and `hosting` your `Minecraft network`.
    However, it's strongly recommended to setup a `test server` in your `local machine`.
    The `test server` should be a `mirror` of that `production server`.
    It should contains the `mods` files and the `config files`.

    You can modify and test new things in your `test server`.
    After everything is configured and working properly, you just upload the `mods` and `config files` into your `remote machine`.
    """)
@ColorBox(id = 1754014854649L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ Open `this` GUI
    Issue: `/fuji`

    ◉ List all `fuji commands`
    Issue: `/fuji inspect fuji-commands`

    ◉ List all `fuji permissions` and `fuji metas`
    Issue: `/fuji inspect permissions-and-metas`

    ◉ List all `fuji placeholders`
    Issue: `/fuji inspect placeholders`

    ◉ List all `fuji configurations`
    Issue: `/fuji inspect configurations`

    ◉ List all `fuji jobs`
    Issue: `/fuji inspect jobs`

    ◉ List all `fuji argument types`
    Issue: `/fuji inspect argument-types`
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
            - To see the overview document, read the `fuji manual` pdf file in [2;34mhttps://github.com/sakurawald/fuji/raw/dev/docs/release/fuji.pdf[0m[2;35m
            - To discover new things, use `/fuji inspect` command.
            - Anything unclear, open an issue in [2;34mhttps://github.com/sakurawald/fuji/issues[0m[2;35m[0m[2;35m
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
        return ServerHelper.getSelfModContainer().getMetadata().getVersion().getFriendlyString();
    }

}

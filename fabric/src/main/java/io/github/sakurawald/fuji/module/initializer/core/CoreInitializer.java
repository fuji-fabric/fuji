package io.github.sakurawald.fuji.module.initializer.core;

import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.config.Configs;
import io.github.sakurawald.fuji.core.event.impl.ServerLifecycleEvents;
import io.github.sakurawald.fuji.core.manager.impl.module.ModuleManager;
import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;

import java.util.ArrayList;
import java.util.List;

@ColorBox(id = 1751870436910L, color = ColorBox.ColorBlockTypes.TIPS, value = """
    ◉ How to use fuji?

    All `modules` are `disabled` by default.
    You can only enable the `interested modules`.
    Modify the `config/fuji/config.json` file to `enable` a module.
    After that, `re-start` the server, to apply the `module enable status`.
    """)

@ColorBox(id = 1751870440489L, color = ColorBox.ColorBlockTypes.NOTE, value = """
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


public class CoreInitializer extends ModuleInitializer {

    @Override
    protected void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> CoreInitializer.onServerStartSuccess());
    }

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
           """;
        LogUtil.info(userGuide);
    }

    private static void tryPrintUserGuide() {
        if (Configs.MAIN_CONTROL_CONFIG.model().core.debug.print_user_guide_in_console) {
            printUserGuide();
        }
    }

    private static void onServerStartSuccess() {
        /* Report enabled/disabled modules. */
        List<String> enabledModuleList = new ArrayList<>();
        ModuleManager.MODULE_ENABLE_STATUS.forEach((module, enable) -> {
            if (enable) enabledModuleList.add(ModuleManager.joinModulePath(module));
        });

        enabledModuleList.sort(String::compareTo);
        LogUtil.info("Enabled {}/{} modules -> {}", enabledModuleList.size(), ModuleManager.MODULE_ENABLE_STATUS.size(), enabledModuleList);

        /* Print the user guide for new users. */
        tryPrintUserGuide();
    }
}

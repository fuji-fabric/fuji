package io.github.sakurawald.module.initializer.core;

import io.github.sakurawald.core.auxiliary.LogUtil;
import io.github.sakurawald.core.config.Configs;
import io.github.sakurawald.core.manager.impl.module.ModuleManager;
import io.github.sakurawald.core.structure.descriptor.annotation.ColorBox;
import io.github.sakurawald.module.initializer.ModuleInitializer;

import java.util.ArrayList;
import java.util.List;

@ColorBox("More color boxes coming.")
public class CoreInitializer extends ModuleInitializer {


    public static void printUserGuide() {
        // NOTE: The generator is https://rebane2001.com/discord-colored-text-generator/
        String userGuide = """
            [2;35m[1;35m
            [Fuji User Guide][0m[2;35m
            It seems that this is the first time you use fuji mod.

            Here are some important points:
            - Fuji is designed to be fully-modular, that is to say, [2;34mall modules are disabled by default.[0m[2;35m
            - To enable a module: modify the `[2;34mconfig/fuji/config.json[0m[2;35m` file, and [2;34mre-start[0m[2;35m the server to apply the modification.
                - To use `/tpa` command, enable the `tpa` module.
                - To use placeholders provided by fuji, enable the `placeholder` module.
                - To use echo commands like `/send-message`, `/send-broadcast` etc, enable the `echo` module.
            - To see the list of modules, and what functionality they provides, read the `fuji manual` pdf file in [2;34mhttps://github.com/sakurawald/fuji/raw/dev/docs/release/fuji.pdf[0m[2;35m
            - To discover new things, use `/fuji inspect` command.
            - Anything unclear, open an issue in [2;34mhttps://github.com/sakurawald/fuji/issues[0m[2;35m[0m[2;35m
            - Now, issue `[2;34m/fuji[0m[2;35m` to get started!
           """;
        LogUtil.info(userGuide);
    }

    public static void tryPrintUserGuide(List<String> enabledModuleList) {
        if (Configs.mainControlConfig.model().core.debug.print_user_guide_in_console
        || enabledModuleList.size() == 1) {
            printUserGuide();
        }
    }

    public static void onServerStartSuccess() {
        /* Report enabled/disabled modules. */
        List<String> enabledModuleList = new ArrayList<>();
        ModuleManager.MODULE_ENABLE_STATUS.forEach((module, enable) -> {
            if (enable) enabledModuleList.add(ModuleManager.joinModulePath(module));
        });

        enabledModuleList.sort(String::compareTo);
        LogUtil.info("Enabled {}/{} modules -> {}", enabledModuleList.size(), ModuleManager.MODULE_ENABLE_STATUS.size(), enabledModuleList);

        /* Print the user guide for new users. */
        tryPrintUserGuide(enabledModuleList);
    }
}

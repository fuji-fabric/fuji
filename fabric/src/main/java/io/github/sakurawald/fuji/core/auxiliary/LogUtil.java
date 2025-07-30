package io.github.sakurawald.fuji.core.auxiliary;

import io.github.sakurawald.fuji.Fuji;
import io.github.sakurawald.fuji.core.config.Configs;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class LogUtil {

    private static final @NotNull Logger MOD_LOGGER = makeLogger(StringUtils.capitalize(Fuji.MOD_ID));

    private static @NotNull Logger makeLogger(String name) {
        return LogManager.getLogger(name);
    }

    private static final boolean IS_CONSOLE_SUPPORTS_ANSI_COLOR = isConsoleSupportAnsiColor();

    private static boolean isConsoleSupportAnsiColor() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER;
    }

    private static String attachSourceModulePrefix(String message) {
        String prefix = "[%s] ".formatted(ReflectionUtil.Stacktrace.findSourceModuleInCurrentStackTrace());
        return prefix + message;
    }

    public static void debug(String message, Object... args) {
        /* Early return for performance. */
        var debugConfig = Configs.MAIN_CONTROL_CONFIG.model().core.debug;
        if (!debugConfig.log_debug_messages) {
            return;
        }

        /* Attach the module info. */
        message = attachSourceModulePrefix(message);

        /* Process the debug config. */
        if (debugConfig.log_debug_messages) {
            String ansiColorPrefix = IS_CONSOLE_SUPPORTS_ANSI_COLOR ? "\u001B[37m" : ""; // Escape for the ansi color code.
            message = ansiColorPrefix + message;
            MOD_LOGGER.info(message, args);
        } else {
            MOD_LOGGER.debug(message, args);
        }
    }

    public static void info(String message, Object... args) {
        message = attachSourceModulePrefix(message);
        MOD_LOGGER.info(message, args);
    }

    public static void warn(String message, Object... args) {
        message = attachSourceModulePrefix(message);
        MOD_LOGGER.warn(message, args);
    }

    public static void error(String message, Object... args) {
        message = attachSourceModulePrefix(message);
        MOD_LOGGER.error(message, args);
    }

}

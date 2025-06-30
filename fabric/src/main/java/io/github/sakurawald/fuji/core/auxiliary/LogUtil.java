package io.github.sakurawald.fuji.core.auxiliary;

import io.github.sakurawald.fuji.Fuji;
import io.github.sakurawald.fuji.core.config.Configs;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.jetbrains.annotations.NotNull;

public class LogUtil {

    private static final @NotNull Logger MOD_LOGGER = makeLogger(StringUtils.capitalize(Fuji.MOD_ID));

    private static @NotNull Logger makeLogger(String name) {
        Logger logger = LogManager.getLogger(name);
        try {
            // You can see the `debug` logs in `logs/debug.txt` file
            String level = System.getProperty("%s.level".formatted(Fuji.MOD_ID));
            Configurator.setLevel(logger, Level.getLevel(level));
        } catch (Exception e) {
            return logger;
        }
        return logger;
    }

    private static final boolean isConsoleSupportAnsiColor = isConsoleSupportAnsiColor();

    private static boolean isConsoleSupportAnsiColor() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER;
    }

    private static String attachSourceModuleInfo(String message) {
        String prefix = "[%s] ".formatted(ReflectionUtil.findSourceModuleInCurrentStackTrace());
        return prefix + message;
    }

    public static void debug(String message, Object... args) {
        /* Early return for performance. */
        var debugConfig = Configs.MAIN_CONTROL_CONFIG.model().core.debug;
        if (!debugConfig.log_debug_messages) {
            return;
        }

        /* Attach the module info. */
        message = attachSourceModuleInfo(message);

        /* Process the debug config. */
        if (debugConfig.log_debug_messages) {
            String prefix = isConsoleSupportAnsiColor ? "\u001B[37m" : ""; // escape for the ansi color code
            String format = prefix + message;
            MOD_LOGGER.info(format, args);
        } else {
            MOD_LOGGER.debug(message, args);
        }
    }

    public static void info(String message, Object... args) {
        message = attachSourceModuleInfo(message);
        MOD_LOGGER.info(message, args);
    }

    public static void warn(String message, Object... args) {
        message = attachSourceModuleInfo(message);
        MOD_LOGGER.warn(message, args);
    }

    public static void error(String message, Object... args) {
        message = attachSourceModuleInfo(message);
        MOD_LOGGER.error(message, args);
    }

}

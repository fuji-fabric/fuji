package io.github.sakurawald.core.auxiliary;

import io.github.sakurawald.Fuji;
import io.github.sakurawald.core.config.Configs;
import io.github.sakurawald.core.manager.impl.module.ModuleManager;
import lombok.experimental.UtilityClass;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

@UtilityClass
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
        String prefix = "[%s] ".formatted(findSourceModuleInCurrentStack());
        return prefix + message;
    }

    public static void debug(String message, Object... args) {
        message = attachSourceModuleInfo(message);

        if (Configs.configHandler.model().core.debug.log_debug_messages
            || FabricLoader.getInstance().isDevelopmentEnvironment()) {
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

    private static List<String> getCurrentStackTraceAsClassNames() {
        return Arrays.stream(Thread.currentThread()
            .getStackTrace())
            .map(StackTraceElement::getClassName)
            .toList();
    }

    private static List<String> getCurrentStackTraceAsModuleName() {
        return getCurrentStackTraceAsClassNames()
            .stream()
            .map(ModuleManager::computeModulePathAsString)
            .toList();
    }

    private static String findSourceModuleInCurrentStack() {
        return findSourceModule(getCurrentStackTraceAsModuleName());
    }

    private static String findSourceModule(List<String> joinedModulePath) {
        String result = "unknown";
        for (String moduleName : joinedModulePath) {
            result = moduleName;
            if (!result.equals(ModuleManager.CORE_MODULE_ROOT)) return result;
        }

        return result;
    }
}

package mod.fuji.core.auxiliary;

import mod.fuji.Fuji;
import mod.fuji.core.auxiliary.minecraft.ServerHelper;
import mod.fuji.core.config.Configs;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class LogUtil {

    public static class AnsiColor {
        public static final String RESET   = "\033[0m";
        public static final String BLACK   = "\033[30m";
        public static final String RED     = "\033[31m";
        public static final String GREEN   = "\033[32m";
        public static final String YELLOW  = "\033[33m";
        public static final String BLUE    = "\033[34m";
        public static final String MAGENTA = "\033[35m";
        public static final String CYAN    = "\033[36m";
        public static final String WHITE   = "\033[37m";

        @SuppressWarnings("SameParameterValue")
        public static @NotNull String wrapAnsiColorCode(@NotNull String contentString, @NotNull String ansiColorCode) {
            contentString = ansiColorCode + contentString + RESET;
            return contentString;
        }
    }

    private static final @NotNull Logger MOD_LOGGER = makeLogger(StringUtils.capitalize(Fuji.MOD_ID));

    private static @NotNull Logger makeLogger(String name) {
        return LogManager.getLogger(name);
    }

    private static final boolean IS_CONSOLE_SUPPORTS_ANSI_COLOR = isConsoleSupportAnsiColor();

    private static boolean isConsoleSupportAnsiColor() {
        return ServerHelper.Environment.isServerSideDedicatedServer();
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

    @SuppressWarnings("unused")
    public static void disabled(String message, Object... args) {
        // This logging method is a dummy method used for disabled logs.
    }

    @SuppressWarnings("unused")
    public static void stdout(String message, Object ... args) {
        System.out.printf(message, args);
    }

}

package io.github.sakurawald.fuji.core.diagnostic;

import io.github.sakurawald.fuji.core.auxiliary.ExceptionUtil;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.diagnostic.analyzer.ExceptionAnalyzer;
import io.github.sakurawald.fuji.core.diagnostic.analyzer.FujiCrashExceptionAnalyzer;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public class DiagnosticEmitter {

    private static @NotNull List<ExceptionAnalyzer> makeExceptionAnalyzers() {
        return List.of(new FujiCrashExceptionAnalyzer());
    }

    public static void analyzeCaptureThrowable(@NotNull Throwable throwable) {
        StringBuilder sb = new StringBuilder();

        makeExceptionAnalyzers()
            .stream()
            .map(it -> it.analyze(throwable))
            .filter(Optional::isPresent)
            .forEach(sb::append);

        List<Throwable> throwableChain = ExceptionUtil.getThrowableChain(throwable);

        LogUtil.warn("""

            [Diagnostic Analyzer]

            ◉ Exception Chain
            {}

            ◉ Exception Time Order
            {}
            """, getCauseChain(throwableChain), getTimeOrder(throwableChain));
    }


    private static @NotNull String getCauseChain(@NotNull List<Throwable> throwableChain) {
        StringBuilder sb = new StringBuilder();
        for (int index = throwableChain.size() - 1; index >= 0; index--) {
            Throwable t = throwableChain.get(index);
            sb.append("[%d]".formatted(index))
                .append(" ")
                .append(t.getClass().getName())
                .append(": ")
                .append(t.getMessage())
                .append(System.lineSeparator());
        }

        return sb.toString();
    }

    private static String getTimeOrder(@NotNull List<Throwable> throwableChain) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < throwableChain.size(); i++) {
            sb.append("[").append(i).append("]");
            if (i < throwableChain.size() - 1) {
                sb.append(" -> ");
            }
        }

        return sb.toString();
    }

}

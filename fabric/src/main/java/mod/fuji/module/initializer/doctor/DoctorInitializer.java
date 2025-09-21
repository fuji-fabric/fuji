package mod.fuji.module.initializer.doctor;

import mod.fuji.core.auxiliary.ExceptionUtil;
import mod.fuji.core.auxiliary.LogUtil;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.module.initializer.ModuleInitializer;
import mod.fuji.module.initializer.doctor.analyzer.ExceptionAnalyzer;
import mod.fuji.module.initializer.doctor.analyzer.FujiModuleMixinApplicationExceptionAnalyzer;
import mod.fuji.module.initializer.doctor.analyzer.GenericModMixinApplicationExceptionAnalyzer;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

@Document(id = 1757426112765L, value = """
    This module provides the `server crash analyzer` when the `server crashed`.
    """)
public class DoctorInitializer extends ModuleInitializer {

    private static @NotNull List<ExceptionAnalyzer> makeExceptionAnalyzers() {
        return List.of(
            new FujiModuleMixinApplicationExceptionAnalyzer(),
            new GenericModMixinApplicationExceptionAnalyzer()
        );
    }

    public static void analyzeCaptureThrowable(@NotNull Throwable throwable) {
        List<Throwable> throwableChain = ExceptionUtil.getThrowableChain(throwable);
        String causeChain = formatCauseChain(throwableChain);

        StringBuilder diagnosisBuilder = new StringBuilder();
        makeExceptionAnalyzers()
            .stream()
            .map(it -> it.analyze(throwableChain, causeChain))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .forEach(diagnosisBuilder::append);
        String diagnosis = diagnosisBuilder.toString();
        if (diagnosis.isBlank()) {
            diagnosis = "None, see logs above for details.";
        }

        String formatString = """

            [Server Crash Analyzer]

            ◉ Cause Chain
            {}
            Time Order: {}

            ◉ Diagnosis
            {}

            ----- Server Program Terminated Here -----
            """;
        formatString = LogUtil.AnsiColor.wrapAnsiColorCode(formatString, LogUtil.AnsiColor.GREEN);

        String timeOrder = formatTimeOrder(throwableChain);
        LogUtil.warn(formatString, causeChain, timeOrder, diagnosis);
    }

    private static @NotNull String formatCauseChain(@NotNull List<Throwable> throwableChain) {
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

    private static String formatTimeOrder(@NotNull List<Throwable> throwableChain) {
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

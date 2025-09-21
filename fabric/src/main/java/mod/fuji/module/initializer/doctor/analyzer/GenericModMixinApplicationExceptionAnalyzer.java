package mod.fuji.module.initializer.doctor.analyzer;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;

public class GenericModMixinApplicationExceptionAnalyzer extends ExceptionAnalyzer {

    @Override
    protected @NotNull Pattern makePattern() {
        return Pattern.compile("mixins.json:(.+?)\\s+?from\\s+?mod\\s+?(.+?)\\b");
    }

    @Override
    public Optional<String> analyze(@NotNull List<Throwable> throwableChain, @NotNull String causeChain) {
        StringBuilder diagnosisBuilder = new StringBuilder();

        Matcher matcher = getPattern().matcher(causeChain);
        while (matcher.find()) {
            String mixinName = matcher.group(1);
            String modName = matcher.group(2);
            diagnosisBuilder
                .append("- [Reason] Failed to apply the mixin '%s' from mod '%s'.".formatted(mixinName, modName))
                .append(System.lineSeparator());
        }

        return Optional.of(diagnosisBuilder.toString());
    }
}

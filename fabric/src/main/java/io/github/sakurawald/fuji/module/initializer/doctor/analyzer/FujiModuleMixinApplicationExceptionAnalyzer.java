package io.github.sakurawald.fuji.module.initializer.doctor.analyzer;

import io.github.sakurawald.fuji.core.auxiliary.ReflectionUtil;
import io.github.sakurawald.fuji.core.manager.impl.module.ModulePathResolver;
import io.github.sakurawald.fuji.module.mixin.GlobalMixinConfigPlugin;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;

public class FujiModuleMixinApplicationExceptionAnalyzer extends ExceptionAnalyzer {

    @Override
    protected @NotNull Pattern makePattern() {
        return Pattern.compile("fuji.mixins.json:(.+?)\\s+from");
    }

    @SuppressWarnings("CollectionAddAllCanBeReplacedWithConstructor")
    @Override
    public Optional<String> analyze(@NotNull List<Throwable> throwableChain, @NotNull String causeChain) {
        Set<String> derivedFQCNs = new HashSet<>();

        /* Collect failed mixin names. */
        Set<String> failedMixinFQCNs = new HashSet<>();
        Matcher matcher = getPattern().matcher(causeChain);
        while (matcher.find()) {
            String mixinName = GlobalMixinConfigPlugin.getMixinRootPackage() + "." + matcher.group(1);
            failedMixinFQCNs.add(mixinName);
        }
        derivedFQCNs.addAll(failedMixinFQCNs);

        /* Resolve the event mixin consumers. */
        failedMixinFQCNs
            .forEach(mixinFQCN -> {
                ReflectionUtil.CompileTimeGraph
                    .getEventGraph()
                    .resolveConsumers(mixinFQCN)
                    .forEach(it -> derivedFQCNs.add(it.getDeclaringClassName()));
            });


        /* Map the FQCNs into module path strings. */
        StringBuilder diagnosisBuilder = new StringBuilder();
        derivedFQCNs
            .stream()
            .map(ModulePathResolver::computeModulePathString)
            .forEach(modulePathString -> {
                diagnosisBuilder.append("- [Solution] Failed to initialize the '%s' module from 'fuji' mod, please try to disable it in 'config/fuji/config.json' and re-start the server..".formatted(modulePathString)).append(System.lineSeparator());
            });

        return Optional.of(diagnosisBuilder.toString());
    }
}

package io.github.sakurawald.fuji.core.document.auxiliary;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.descriptor.CommandDescriptor;
import io.github.sakurawald.fuji.core.command.processor.CommandAnnotationProcessor;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.document.structure.DocString;
import io.github.sakurawald.fuji.core.manager.impl.module.ModuleManager;
import io.github.sakurawald.fuji.core.manager.impl.scheduler.ScheduleManager;
import io.github.sakurawald.fuji.core.service.url_highlighter.UrlHighlighter;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.core.document.structure.JobDescriptor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.matchers.GroupMatcher;

public class DocumentUtil {

    @SuppressWarnings("UnnecessaryLocalVariable")
    public static @NotNull String getDocString(@Nullable Object audience, long id) {
        if (id == 0) return "DUMMY-DOC-STRING";

        String languageKey = DocString.DOC_STRING_KEY_PREFIX + id;
        // NOTE: For doc string, we always use the default language to display language values.
        String languageValue = TextHelper.Translator.getLanguageValueByKey(audience, languageKey);
        return languageValue;
    }

    private static @Nullable String getDocumentString(@Nullable Object audience, @Nullable Document annotation) {
        if (annotation == null) {
            return null;
        }

        /* Always provide the latest version for en_US users. */
        if (TextHelper.Loader.shouldUseBuiltInDocStrings()) {
            return annotation.value();
        }

        /* Retrieve the doc string from language file. */
        return getDocString(audience, annotation.id());
    }

    public static @Nullable String getColorBoxString(@Nullable Object audience, @Nullable ColorBox annotation) {
        if (annotation == null) {
            return null;
        }

        /* Always provide the latest version for en_US users. */
        if (TextHelper.Loader.shouldUseBuiltInDocStrings()) {
            return annotation.value();
        }

        /* Retrieve the doc string from language file. */
        return getDocString(audience, annotation.id());
    }

    public static Optional<String> getClassDocumentString(@Nullable Object audience, @NotNull Class<?> clazz) {
        Document annotation = clazz.getAnnotation(Document.class);
        return Optional.ofNullable(getDocumentString(audience, annotation));
    }

    public static @Nullable String getFieldDocumentString(@Nullable Object audience, @NotNull Field field) {
        Document annotation = field.getAnnotation(Document.class);
        return getDocumentString(audience, annotation);
    }

    public static @NotNull String compileDocumentString(@NotNull String documentString) {
        /* Adds the color prefix for each line. */
        return Arrays
            .stream(documentString.split("\n"))
            .map(DocumentUtil::compileDocumentStringLine)
            .collect(Collectors.joining("\n"));
    }

    private static @NotNull String compileDocumentStringLine(@NotNull String line) {
        if (line.startsWith("◉")) {
            line = "<bold>" + line;
        }

        line = line.replaceAll("\\*\\*(.+?)\\*\\*", "<bold>$1</bold>");

        line = line.replace("*", "\\*");

        line = UrlHighlighter.highlight(line);

        line = line.replaceAll("`/(.+?)`", "<gold>/$1</gold>");
        line = line.replaceAll("`(.+?)`", "<grey>$1</grey>");

        line = line.replaceAll("%(.+?:.+?)%", "<aqua>%$1%</aqua>");

        line = line.replaceAll("Alex", "<dark_green>Alex</dark_green>");
        line = line.replaceAll("Steve", "<dark_green>Steve</dark_green>");

        line = line.replaceAll("Alice", "<dark_green>Alice</dark_green>");
        line = line.replaceAll("Bob", "<dark_green>Bob</dark_green>");
        line = line.replaceAll("Carol", "<dark_green>Carol</dark_green>");
        line = line.replaceAll("Dave", "<dark_green>Dave</dark_green>");

        line = line.replaceAll("@a", "<#FF0000>@a</#FF0000>");
        line = line.replaceAll("@e", "<#FF0000>@e</#FF0000>");
        line = line.replaceAll("@n", "<#FF0000>@n</#FF0000>");
        line = line.replaceAll("@p", "<#FF0000>@p</#FF0000>");
        line = line.replaceAll("@r", "<#FF0000>@r</#FF0000>");
        line = line.replaceAll("@s", "<#FF0000>@s</#FF0000>");

        line = line.replaceAll("If ", "<#B13BFF>If </#B13BFF>");
        line = line.replaceAll("if ", "<#B13BFF>if </#B13BFF>");
        line = line.replaceAll("then ", "<#B13BFF>then </#B13BFF>");
        line = line.replaceAll("else ", "<#B13BFF>else </#B13BFF>");

        line = line.replaceAll("\\bIF\\b", "<blue>IF</blue>");
        line = line.replaceAll("\\bTHEN\\b", "<blue>THEN</blue>");
        line = line.replaceAll("\\bELSE\\b", "<blue>ELSE</blue>");

        line = line.replaceAll("^(\\d\\. )", "<#87CEEB>$1</#87CEEB>");
        line = line.replaceAll("^(\\d\\.[a-z]\\. )", "<#87CEEB>$1</#87CEEB>");
        line = line.replaceAll("^(\\d\\.[a-z]\\.\\w+?\\. )", "<#87CEEB>$1</#87CEEB>");

        line = line.replaceAll("\\\\<(.+?)\\\\>", "<yellow>\\\\<$1\\\\></yellow>");

        line = TextHelper.Fixer.fixParserInput(line);
        return "<#FFA1F5>" + line;
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    public static @NotNull List<ColorBox> getColorBoxes(@NotNull String modulePathString) {
        /* Get the module initializer class. */
        Class<? extends ModuleInitializer> moduleInitializerClass = ModuleManager.MODULE_INITIALIZER_CLASS_BY_MODULE_PATH_STRING
            .get(modulePathString);
        if (moduleInitializerClass == null) return List.of();

        /* Iterate the color boxes. */
        ColorBox[] boxes = moduleInitializerClass
            .getDeclaredAnnotationsByType(ColorBox.class);

        /* Sort the color box by its colors. */
        List<ColorBox> colorBoxes = Arrays
            .stream(boxes)
            .sorted(Comparator.comparing(ColorBox::color)
                .reversed())
            .toList();

        return colorBoxes;
    }

    public static @NotNull List<BaseConfigurationHandler<?>> getObjectConfigurationHandlers() {
        return BaseConfigurationHandler.REGISTERED_CONFIGURATION_HANDLERS
            .stream()
            .filter(it -> it instanceof ObjectConfigurationHandler<?>)
            .sorted(Comparator.comparing(BaseConfigurationHandler::getFilePath))
            .toList();
    }

    public static @NotNull List<JobDescriptor> getJobDescriptors() throws SchedulerException {
        List<JobDescriptor> entities = new ArrayList<>();

        /* Get all jobs. */
        Scheduler scheduler = ScheduleManager.getScheduler();

        // NOTE: Match all jobs, including `CronJob` and `FixedIntervalJob`.
        GroupMatcher<JobKey> jobKeyGroupMatcher = GroupMatcher.anyJobGroup();
        Set<JobKey> jobKeys = scheduler.getJobKeys(jobKeyGroupMatcher);
        for (JobKey jobKey : jobKeys) {
            JobDetail jobDetail = scheduler.getJobDetail(jobKey);
            List<? extends Trigger> triggersOfJob = scheduler.getTriggersOfJob(jobKey);
            entities.add(new JobDescriptor(jobDetail, triggersOfJob));
        }

        entities.sort(Comparator.comparing(it -> it.getJobDetail().getKey().getGroup()));
        return entities;
    }

    public static @NotNull List<CommandDescriptor> getCommandDescriptors() {
        return CommandAnnotationProcessor
            .REGISTERED_COMMAND_DESCRIPTORS
            .stream()
            .sorted(Comparator.comparing(CommandDescriptor::getCommandNodePath))
            .toList();
    }
}

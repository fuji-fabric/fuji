package io.github.sakurawald.fuji.core.document.auxiliary;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.document.structure.DocString;
import io.github.sakurawald.fuji.core.service.url_highlighter.UrlHighlighter;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    public static @Nullable String getClassDocumentString(@Nullable Object audience, Class<?> clazz) {
        Document annotation = clazz.getAnnotation(Document.class);
        return getDocumentString(audience, annotation);
    }

    public static @Nullable String getFieldDocumentString(@Nullable Object audience, Field field) {
        Document annotation = field.getAnnotation(Document.class);
        return getDocumentString(audience, annotation);
    }

    public static String compileDocumentString(String documentString) {
        /* Adds the color prefix for each line. */
        return Arrays
            .stream(documentString.split("\n"))
            .map(DocumentUtil::compileDocumentStringLine)
            .collect(Collectors.joining("\n"));
    }

    private static @NotNull String compileDocumentStringLine(String line) {
        if (line.startsWith("◉")) {
            line = "<bold>" + line;
        }

        line = line.replaceAll("`/(.+?)`", "<gold>/$1</gold>");

        line = line.replaceAll("`(.+?)`", "<grey>$1</grey>");

        line = line.replaceAll("%(.+?)%", "<aqua>%$1%</aqua>");

        line = line.replaceAll("Alice", "<dark_green>Alice</dark_green>");
        line = line.replaceAll("Bob", "<dark_green>Bob</dark_green>");
        line = line.replaceAll("Carol", "<dark_green>Carol</dark_green>");
        line = line.replaceAll("Dave", "<dark_green>Dave</dark_green>");
        line = line.replaceAll("Eve", "<dark_green>Eve</dark_green>");

        line = line.replaceAll("@a ", "<pink>@a </pink>");
        line = line.replaceAll("@e ", "<pink>@e </pink>");
        line = line.replaceAll("@n ", "<pink>@n </pink>");
        line = line.replaceAll("@p ", "<pink>@p </pink>");
        line = line.replaceAll("@r ", "<pink>@r </pink>");
        line = line.replaceAll("@s ", "<pink>@s </pink>");

        line = line.replaceAll("\\\\<(.+?)\\\\>", "<yellow>\\\\<$1\\\\></yellow>");

        line = UrlHighlighter.highlight(line);

        return "<#FFA1F5>" + line;
    }
}

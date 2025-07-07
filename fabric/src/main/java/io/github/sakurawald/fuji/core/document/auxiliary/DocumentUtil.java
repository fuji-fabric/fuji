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
    private static @NotNull String getDocString(@Nullable Object audience, long id) {
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
        if (TextHelper.Loader.isDefaultLanguageCodeEnUS()) {
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
        if (TextHelper.Loader.isDefaultLanguageCodeEnUS()) {
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
        String decoratedDocumentString = Arrays
            .stream(documentString.split("\n"))
            .map(line -> "<#FFA1F5>" + line)
            .collect(Collectors.joining("\n"));

        /* Highlight the URL links in document string. */
        decoratedDocumentString = UrlHighlighter.highlight(decoratedDocumentString);

        return decoratedDocumentString;
    }
}

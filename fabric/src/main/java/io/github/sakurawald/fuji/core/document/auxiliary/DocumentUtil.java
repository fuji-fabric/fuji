package io.github.sakurawald.fuji.core.document.auxiliary;

import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.service.url_highlighter.UrlHighlighter;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.jetbrains.annotations.Nullable;

public class DocumentUtil {

    private static @Nullable String getDocumentString(Document annotation) {
        if (annotation != null) {
            return annotation.value();
        }

        return null;
    }

    public static @Nullable String getClassDocumentString(Class<?> clazz) {
        Document annotation = clazz.getAnnotation(Document.class);
        return getDocumentString(annotation);
    }

    public static @Nullable String getFieldDocumentString(Field field) {
        Document annotation = field.getAnnotation(Document.class);
        return getDocumentString(annotation);
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

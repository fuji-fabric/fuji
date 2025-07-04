package io.github.sakurawald.fuji.core.document.auxiliary;

import io.github.sakurawald.fuji.core.document.annotation.Document;
import java.lang.reflect.Field;
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
}

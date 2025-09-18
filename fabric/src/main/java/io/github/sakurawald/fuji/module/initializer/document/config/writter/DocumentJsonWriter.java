package io.github.sakurawald.fuji.module.initializer.document.config.writter;

import com.google.gson.FormattingStyle;
import com.google.gson.stream.JsonWriter;
import io.github.sakurawald.fuji.core.auxiliary.ReflectionUtil;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.document.auxiliary.DocumentUtil;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public class DocumentJsonWriter extends JsonWriter {

    private final JsonWriter delegate;
    private final Map<String, String> documentStringMap;

    public DocumentJsonWriter(JsonWriter delegate, Map<String, String> documentStringMap) {
        super(getBackendWriter(delegate));
        this.delegate = delegate;
        this.documentStringMap = documentStringMap;

        /* Tweak the variables initialized in the constructor function. */
        this.setFormattingStyle(FormattingStyle.PRETTY);
    }

    @Override
    public JsonWriter name(String name) throws IOException {
        /* Append the document string before the Json property name (comma + name). */
        Optional<String> documentString = Optional.ofNullable(documentStringMap.get(name));
        if (documentString.isPresent()) {
            Writer underlyingWriter = getBackendWriter(delegate);
            FormattingStyle formattingStyle = getFormattingStyle();

            String formattedDocumentString = documentString.get().trim();
            formattedDocumentString = DocumentUtil.Indenter.indentExceptFirstLine(formattedDocumentString, formattingStyle.getIndent());

            underlyingWriter
                .append(formattingStyle.getNewline())
                .append(formattingStyle.getIndent()).append("/* ").append(formattedDocumentString).append(" */");
        }

        /* Call super to handle default logics. */
        return super.name(name);
    }

    private static @NotNull Writer getBackendWriter(@NotNull JsonWriter jsonWriter) {
        return ReflectionUtil.Reflection.getInstanceFieldValue(jsonWriter, "out", Writer.class);
    }

}

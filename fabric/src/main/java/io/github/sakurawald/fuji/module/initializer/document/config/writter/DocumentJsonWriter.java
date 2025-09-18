package io.github.sakurawald.fuji.module.initializer.document.config.writter;

import com.google.gson.stream.JsonWriter;
import io.github.sakurawald.fuji.core.auxiliary.ReflectionUtil;
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
        setPrettyFormatting();
    }

    private static void setPrettyFormatting() {
        #if MC_VER >= MC_1_21_4
        this.setFormattingStyle(com.google.gson.FormattingStyle.PRETTY);
        #endif
    }

    @Override
    public JsonWriter name(String name) throws IOException {
        /* Append the document string before the Json property name (comma + name). */
        Optional<String> documentString = Optional.ofNullable(documentStringMap.get(name));
        if (documentString.isPresent()) {
            Writer underlyingWriter = getBackendWriter(delegate);

            String formattedDocumentString = documentString.get().trim();
            formattedDocumentString = DocumentUtil.Indenter.indentExceptFirstLine(formattedDocumentString, getIndent());

            underlyingWriter
                .append(getLineSeperator())
                .append(getIndent()).append("/* ").append(formattedDocumentString).append(" */");
        }

        /* Call super to handle default logics. */
        return super.name(name);
    }

    private static @NotNull Writer getBackendWriter(@NotNull JsonWriter jsonWriter) {
        return ReflectionUtil.Reflection.getInstanceFieldValue(jsonWriter, "out", Writer.class);
    }

    private @NotNull String getIndent() {
        #if MC_VER < MC_1_21_4
        return ReflectionUtil.Reflection.getInstanceFieldValue(this, "indent", String.class);
        #elif MC_VER >= MC_1_21_4
        com.google.gson.FormattingStyle formattingStyle = getFormattingStyle();
        return formattingStyle.getIndent();
        #endif
    }


    private @NotNull String getLineSeperator() {
        #if MC_VER < MC_1_21_4
        return ReflectionUtil.Reflection.getInstanceFieldValue(this, "separator", String.class);
        #elif MC_VER >= MC_1_21_4
        com.google.gson.FormattingStyle formattingStyle = getFormattingStyle();
        return formattingStyle.getNewLine();
        #endif
    }

}

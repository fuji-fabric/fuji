package mod.fuji.module.initializer.document.config.writter;

import com.google.gson.stream.JsonWriter;
import mod.fuji.core.auxiliary.ReflectionUtil;
import mod.fuji.core.document.auxiliary.DocumentUtil;
import java.io.IOException;
import java.io.Writer;
import java.util.Optional;
import mod.fuji.module.initializer.document.config.adapter.DocumentedTypeAdapterFactory;
import org.jetbrains.annotations.NotNull;

public class DocumentJsonWriter extends JsonWriter {

    private final JsonWriter delegate;

    public DocumentJsonWriter(JsonWriter delegate) {
        super(getBackendWriter(delegate));
        this.delegate = delegate;

        /* Tweak the variables initialized in the constructor function. */
        setPrettyFormatting();
    }

    private void setPrettyFormatting() {
        #if MC_VER >= MC_1_21_4
        this.setFormattingStyle(com.google.gson.FormattingStyle.PRETTY);
        #endif
    }

    @Override
    public JsonWriter name(String name) throws IOException {
        /* Append the document string before the Json property name (comma + name). */
        Optional<String> documentString = Optional.ofNullable(DocumentedTypeAdapterFactory.mostRecentlyDocumentStringMap.get(name));
        if (documentString.isPresent()) {
            Writer underlyingWriter = getBackendWriter(delegate);

            String formattedDocumentString = documentString.get().trim();
            formattedDocumentString = DocumentUtil.Indenter.indentExceptFirstLine(formattedDocumentString, getCurrentIndent());

            underlyingWriter
                .append(getLineSeparator())
                .append(getCurrentIndent()).append("/* ").append(formattedDocumentString).append(" */");
        }

        /* Call super to handle default logics. */
        return super.name(name);
    }

    private static @NotNull Writer getBackendWriter(@NotNull JsonWriter jsonWriter) {
        return ReflectionUtil.Reflection.getInstanceFieldValue(jsonWriter, "out", Writer.class);
    }

    @SuppressWarnings("StringRepeatCanBeUsed")
    private @NotNull String getCurrentIndent() {
        StringBuilder builder = new StringBuilder();
        String indent = getIndent();
        for (int i = 1; i < getStackSize(); i++) {
            builder.append(indent);
        }
        return builder.toString();
    }

    private @NotNull String getIndent() {
        #if MC_VER < MC_1_21_4
        return ReflectionUtil.Reflection.getInstanceFieldValue(this, "indent", String.class);
        #elif MC_VER >= MC_1_21_4
        com.google.gson.FormattingStyle formattingStyle = getFormattingStyle();
        return formattingStyle.getIndent();
        #endif
    }


    private @NotNull String getLineSeparator() {
        #if MC_VER < MC_1_21_4
        return ReflectionUtil.Reflection.getInstanceFieldValue(this, "separator", String.class);
        #elif MC_VER >= MC_1_21_4
        com.google.gson.FormattingStyle formattingStyle = getFormattingStyle();
        return formattingStyle.getNewline();
        #endif
    }

    private int getStackSize() {
        return ReflectionUtil.Reflection.getInstanceFieldValue(this, "stackSize", Integer.class);
    }


}

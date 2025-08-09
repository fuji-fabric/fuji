package io.github.sakurawald.fuji.core.document.structure;

import io.github.sakurawald.fuji.core.document.interfaces.DocStringLike;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("ClassCanBeRecord")
@Data
public class DocString implements DocStringLike {
    public static final String DOC_STRING_KEY_PREFIX = "docstring.";

    final long id;
    final String value;

    public static long parseDocStringId(@NotNull String jsonKey) {
        String substring = jsonKey.substring(DOC_STRING_KEY_PREFIX.length());
        return Long.parseLong(substring);
    }

}

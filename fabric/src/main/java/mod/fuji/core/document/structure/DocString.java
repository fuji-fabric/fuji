package mod.fuji.core.document.structure;

import mod.fuji.core.document.interfaces.DocStringLike;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("ClassCanBeRecord")
@Value
public class DocString implements DocStringLike {
    public static final String DOC_STRING_KEY_PREFIX = "docstring.";
    public static final int DUMMY_DOC_STRING_ID = 0;

    long id;
    String value;

    public static long parseDocStringId(@NotNull String jsonKey) {
        String substring = jsonKey.substring(DOC_STRING_KEY_PREFIX.length());
        return Long.parseLong(substring);
    }

}

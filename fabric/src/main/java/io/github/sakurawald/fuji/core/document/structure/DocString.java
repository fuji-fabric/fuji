package io.github.sakurawald.fuji.core.document.structure;

import lombok.Data;

@Data
public class DocString {
    public static final String DOC_STRING_KEY_PREFIX = "docstring.";

    private final long id;
    private final String value;

    public static long parseDocStringId(String jsonKey) {
        String substring = jsonKey.substring(DOC_STRING_KEY_PREFIX.length());
        return Long.parseLong(substring);
    }

}

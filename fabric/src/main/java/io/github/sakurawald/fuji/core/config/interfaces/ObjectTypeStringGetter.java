package io.github.sakurawald.fuji.core.config.interfaces;

import io.github.sakurawald.fuji.core.document.annotation.ForDeveloper;
import org.jetbrains.annotations.NotNull;

@ForDeveloper("""
    This interface provides the method to get the `object type string`.

    The returned type string should not be the simple class name of the object.
    Since the class name is very likely to be re-named.

    The returned type string is something that will be serialized and de-serialized.

    You can use a enum class to describe the possible values for the type string.

    After that, you should register a custom gson type adapter, to dispatch the object type string, and use different concrete class to make the instance.
    """)
public interface ObjectTypeStringGetter {

    String TYPE_KEY = "type";

    @NotNull String getObjectTypeString();
}

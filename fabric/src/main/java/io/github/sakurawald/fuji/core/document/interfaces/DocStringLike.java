package io.github.sakurawald.fuji.core.document.interfaces;

import io.github.sakurawald.fuji.core.document.annotation.ForDeveloper;

@ForDeveloper("""
    To attach the document information to the objects, you have these tools:
    1. Document
    2. DocStringLike
    3. DocStringProvider
    4. ColorBox
    """)
public interface DocStringLike {
    long getId();
    String getValue();
}

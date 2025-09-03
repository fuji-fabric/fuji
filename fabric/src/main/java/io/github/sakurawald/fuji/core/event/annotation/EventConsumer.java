package io.github.sakurawald.fuji.core.event.annotation;

import com.google.errorprone.annotations.Keep;
import io.github.sakurawald.fuji.core.document.annotation.ForDeveloper;

@Keep
public @interface EventConsumer {

    @ForDeveloper("Event consumers are sorted by natural order.")
    int priority() default 1000;
}

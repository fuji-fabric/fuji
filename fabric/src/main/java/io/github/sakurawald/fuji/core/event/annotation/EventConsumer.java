package io.github.sakurawald.fuji.core.event.annotation;

import com.google.errorprone.annotations.Keep;
import io.github.sakurawald.fuji.core.document.annotation.ForDeveloper;

@Keep
@SuppressWarnings("unused")
public @interface EventConsumer {

    int LOWEST = 0;
    int LOWER = 999;
    int DEFAULT = 1000;
    int HIGHER = 1001;
    int HIGHEST = 2000;

    @ForDeveloper("The mixin priority for mixin injector, matched exactly.")
    int injectorPriority() default DEFAULT;

    @ForDeveloper("Event consumers are sorted by natural order.")
    int consumerPriority() default DEFAULT;
}

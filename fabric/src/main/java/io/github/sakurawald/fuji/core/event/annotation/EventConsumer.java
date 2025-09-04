package io.github.sakurawald.fuji.core.event.annotation;

import com.google.errorprone.annotations.Keep;
import io.github.sakurawald.fuji.core.document.annotation.ForDeveloper;

@Keep
public @interface EventConsumer {

    @ForDeveloper("The injection priority for each mixin injector.")
    int injectorPriority() default 1000;

    @ForDeveloper("Event consumers are sorted by natural order.")
    int consumerPriority() default 1000;
}

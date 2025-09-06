package io.github.sakurawald.fuji.core.event.annotation;

import com.google.errorprone.annotations.Keep;
import io.github.sakurawald.fuji.core.document.annotation.ForDeveloper;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Keep
@SuppressWarnings("unused")
public @interface EventConsumer {

    int LOWEST = 0;
    int LOWER = 999;
    int DEFAULT = 1000;
    int HIGHER = 1001;
    int HIGHEST = 2000;

    @ForDeveloper("""
        If the value is specified to Void.class, then the event graph maker will use the first method parameter as the event type.
        """)
    Class<?> eventType() default Void.class;

    @ForDeveloper("The mixin priority for mixin injector, matched exactly.")
    int injectorPriority() default DEFAULT;

    @ForDeveloper("Event consumers are sorted by natural order.")
    int consumerPriority() default DEFAULT;
}

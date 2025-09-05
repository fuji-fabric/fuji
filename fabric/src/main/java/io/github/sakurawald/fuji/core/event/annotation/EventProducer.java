package io.github.sakurawald.fuji.core.event.annotation;

import com.google.errorprone.annotations.Keep;
import io.github.sakurawald.fuji.core.event.abst.BaseEvent;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
@Keep
public @interface EventProducer {

    Class<? extends BaseEvent> value();

    int injectorPriority() default EventConsumer.DEFAULT;
}

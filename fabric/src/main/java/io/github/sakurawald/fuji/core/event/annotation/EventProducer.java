package io.github.sakurawald.fuji.core.event.annotation;

import com.google.errorprone.annotations.Keep;
import io.github.sakurawald.fuji.core.event.abst.BaseEvent;

@Keep
public @interface EventProducer {

    Class<? extends BaseEvent> value();

    int injectorPriority() default EventConsumer.DEFAULT;
}

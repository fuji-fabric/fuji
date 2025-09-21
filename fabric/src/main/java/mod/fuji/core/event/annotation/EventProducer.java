package mod.fuji.core.event.annotation;

import com.google.errorprone.annotations.Keep;
import mod.fuji.core.event.message.BaseEvent;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE, ElementType.METHOD})
@Repeatable(value = EventProducers.class)
@Keep
public @interface EventProducer {

    Class<? extends BaseEvent> value();

    int injectorPriority() default EventConsumer.DEFAULT;
}

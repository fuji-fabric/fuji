package io.github.sakurawald.fuji.core.command.annotation;

import com.google.errorprone.annotations.Keep;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Keep
public @interface CommandNode {

    String value() default "";

    boolean topLevel() default false;

}

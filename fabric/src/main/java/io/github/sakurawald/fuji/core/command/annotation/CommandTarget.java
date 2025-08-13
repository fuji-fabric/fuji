package io.github.sakurawald.fuji.core.command.annotation;

import io.github.sakurawald.fuji.core.document.annotation.ForDeveloper;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@ForDeveloper("""
    The parameter annotated with this annotation, will be treated as Collection<ServerPlayerEntity>
    """)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface CommandTarget {
}

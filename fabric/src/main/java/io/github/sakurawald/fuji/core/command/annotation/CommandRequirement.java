package io.github.sakurawald.fuji.core.command.annotation;


import io.github.sakurawald.fuji.core.document.annotation.ForDeveloper;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@ForDeveloper("""
    The predicates are tested using an OR clause, instead of AND clause.
    """)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface CommandRequirement {

    String string() default "";

    int level() default 0;
}

package io.github.sakurawald.fuji.core.document.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Document {
    long id();
    String value();
}

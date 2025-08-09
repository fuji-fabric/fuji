package io.github.sakurawald.fuji.core.document.annotation;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.CLASS)
@Repeatable(value = TestCases.class)
public @interface TestCase {
    String steps();
    String[] purposes();
}

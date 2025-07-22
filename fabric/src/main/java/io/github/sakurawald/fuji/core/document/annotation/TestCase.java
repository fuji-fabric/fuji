package io.github.sakurawald.fuji.core.document.annotation;

import java.lang.annotation.Repeatable;

@Repeatable(value = TestCases.class)
public @interface TestCase {
    String value();
}

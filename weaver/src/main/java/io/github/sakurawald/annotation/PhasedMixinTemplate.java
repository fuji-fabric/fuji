package io.github.sakurawald.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface PhasedMixinTemplate {
    String[] suffixes() default {"_LOWEST", "_LOWER", "_DEFAULT", "_HIGHER", "_HIGHEST"};

    int[] priorities() default {0, 999, 1000, 1001, 2000};

    String mixinTypeFqcn() default "org.spongepowered.asm.mixin.Mixin";

    String priorityElement() default "priority";
}

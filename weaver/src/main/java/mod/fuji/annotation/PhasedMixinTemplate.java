package mod.fuji.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
// NOTE: The AP must emit the .java file for each generated mixin class, or the @Unique field initializer will not be transplanted into the target mixin class. (Due to the missing of `remap file`)
public @interface PhasedMixinTemplate {

    String[] suffixes() default {"_LOWEST", "_LOWER", "_DEFAULT", "_HIGHER", "_HIGHEST"};

    int[] priorities() default {0, 999, 1000, 1001, 2000};

}

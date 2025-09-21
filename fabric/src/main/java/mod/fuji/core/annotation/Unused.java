package mod.fuji.core.annotation;

import com.google.errorprone.annotations.Keep;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Keep
@Retention(RetentionPolicy.RUNTIME)
public @interface Unused {

}

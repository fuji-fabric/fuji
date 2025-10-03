package mod.fuji.core.command.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *     The parameter annotated with this annotation, will be treated as Collection<ServerPlayerEntity>

 **/
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface CommandTarget {
}

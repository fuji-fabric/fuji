package mod.fuji.core.command.extension;

import java.util.function.Predicate;

public interface CommandNodeExtension<S> {

    @SuppressWarnings("unused")
    Predicate<S> fuji$getRequirement();

    void fuji$setRequirement(Predicate<S> requirement);

}

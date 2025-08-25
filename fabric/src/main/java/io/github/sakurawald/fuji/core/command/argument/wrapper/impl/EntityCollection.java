package io.github.sakurawald.fuji.core.command.argument.wrapper.impl;

import io.github.sakurawald.fuji.core.command.argument.wrapper.abst.SingularValue;
import java.util.Collection;
import net.minecraft.entity.Entity;

public class EntityCollection extends SingularValue<Collection<? extends Entity>> {
    public EntityCollection(Collection<? extends Entity> value) {
        super(value);
    }
}

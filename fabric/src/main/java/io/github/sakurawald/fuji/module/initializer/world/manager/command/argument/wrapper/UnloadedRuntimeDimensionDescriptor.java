package io.github.sakurawald.fuji.module.initializer.world.manager.command.argument.wrapper;

import io.github.sakurawald.fuji.core.command.argument.wrapper.abst.SingularValue;
import io.github.sakurawald.fuji.module.initializer.world.manager.structure.RuntimeDimensionDescriptor;

public class UnloadedRuntimeDimensionDescriptor extends SingularValue<RuntimeDimensionDescriptor> {
    public UnloadedRuntimeDimensionDescriptor(RuntimeDimensionDescriptor value) {
        super(value);
    }
}

package mod.fuji.module.initializer.world.manager.command.argument.wrapper;

import mod.fuji.core.command.argument.wrapper.abst.SingularValue;
import mod.fuji.module.initializer.world.manager.structure.RuntimeDimensionDescriptor;

public class UnloadedRuntimeDimensionDescriptor extends SingularValue<RuntimeDimensionDescriptor> {
    public UnloadedRuntimeDimensionDescriptor(RuntimeDimensionDescriptor value) {
        super(value);
    }
}

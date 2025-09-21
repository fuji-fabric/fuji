package mod.fuji.module.initializer.world.manager.command.argument.wrapper;

import mod.fuji.core.command.argument.wrapper.abst.SingularValue;
import mod.fuji.module.initializer.world.manager.structure.RuntimeDimensionDescriptor;

public class LoadedRuntimeDimensionDescriptor extends SingularValue<RuntimeDimensionDescriptor> {
    public LoadedRuntimeDimensionDescriptor(RuntimeDimensionDescriptor value) {
        super(value);
    }
}

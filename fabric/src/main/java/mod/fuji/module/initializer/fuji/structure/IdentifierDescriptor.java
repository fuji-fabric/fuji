package mod.fuji.module.initializer.fuji.structure;

import lombok.Data;
import mod.fuji.core.structure.IdentifierIR;

@Data
public class IdentifierDescriptor {
    private final IdentifierIR identifier;
    private final boolean isDynamic;
}

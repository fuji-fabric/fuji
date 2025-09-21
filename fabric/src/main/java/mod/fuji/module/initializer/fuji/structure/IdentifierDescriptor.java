package mod.fuji.module.initializer.fuji.structure;

import lombok.Data;
import net.minecraft.util.Identifier;

@Data
public class IdentifierDescriptor {
    private final Identifier identifier;
    private final boolean isDynamic;
}

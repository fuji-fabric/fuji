package mod.fuji.module.initializer.fuji.structure;

import lombok.Data;
import net.minecraft.resources.ResourceLocation;

@Data
public class IdentifierDescriptor {
    private final ResourceLocation identifier;
    private final boolean isDynamic;
}

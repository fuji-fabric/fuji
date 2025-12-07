package mod.fuji.core.structure;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(staticName = "of")
@Getter
public class IdentifierWrapper {

    #if MC_VER < MC_1_21_11
    net.minecraft.resources.ResourceLocation
    #elif MC_VER >= MC_1_21_11
    net.minecraft.resources.Identifier
    #endif nativeType;

    @Override
    public String toString() {
        return this.getNativeType().toString();
    }
}

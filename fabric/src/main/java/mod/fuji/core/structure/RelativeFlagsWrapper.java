package mod.fuji.core.structure;

import java.util.Set;
import lombok.Value;

@Value(staticConstructor = "of")
public class RelativeFlagsWrapper {

   #if MC_VER <= MC_1_21
   Set<net.minecraft.world.entity.RelativeMovement>
   #elif MC_VER > MC_1_21
   Set<net.minecraft.world.entity.Relative>
   #endif flags;

   public static RelativeFlagsWrapper empty() {
       return RelativeFlagsWrapper.of(Set.of());
   }

}

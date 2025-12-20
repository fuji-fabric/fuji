package mod.fuji.core.structure;

import java.util.Set;
import lombok.Value;

@Value(staticConstructor = "of")
public class RelativeFlagsIR {

   #if MC_VER <= MC_1_21
   Set<net.minecraft.world.entity.RelativeMovement>
   #elif MC_VER > MC_1_21
   Set<net.minecraft.world.entity.Relative>
   #endif flags;

   public static RelativeFlagsIR empty() {
       return RelativeFlagsIR.of(Set.of());
   }

}

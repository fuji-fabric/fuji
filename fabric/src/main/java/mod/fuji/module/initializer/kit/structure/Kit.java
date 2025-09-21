package mod.fuji.module.initializer.kit.structure;

import lombok.Value;
import lombok.With;
import net.minecraft.item.ItemStack;

import java.util.List;

@Value
@With
public class Kit {
    String name;
    List<ItemStack> stackList;
}

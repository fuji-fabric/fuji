package io.github.sakurawald.module.initializer.kit.structure;

import lombok.Data;
import lombok.With;
import net.minecraft.item.ItemStack;

import java.util.List;

@Data
@With
public class Kit {
    final String name;
    final List<ItemStack> stackList;
}

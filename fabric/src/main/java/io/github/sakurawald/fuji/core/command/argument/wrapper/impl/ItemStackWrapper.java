package io.github.sakurawald.fuji.core.command.argument.wrapper.impl;

import lombok.Data;
import net.minecraft.item.ItemStack;

@Data
public class ItemStackWrapper {
    final ItemStack itemStack;
    final String inputString;
}

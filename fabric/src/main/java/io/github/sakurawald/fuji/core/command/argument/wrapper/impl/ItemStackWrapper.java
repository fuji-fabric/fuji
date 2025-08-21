package io.github.sakurawald.fuji.core.command.argument.wrapper.impl;

import lombok.Value;
import net.minecraft.item.ItemStack;

@Value
public class ItemStackWrapper {
    ItemStack itemStack;
    String inputString;
}

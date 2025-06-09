package io.github.sakurawald.module.initializer.head.config.model;

import io.github.sakurawald.module.initializer.head.structure.EconomyType;
import org.jetbrains.annotations.NotNull;

public class HeadConfigModel {
    public EconomyType economy_type = EconomyType.ITEM;
    public @NotNull String cost_type = "minecraft:emerald_block";
    public int cost_amount = 1;
}

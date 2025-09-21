package mod.fuji.module.initializer.head.config.model;

import com.google.gson.annotations.SerializedName;
import mod.fuji.module.initializer.head.structure.EconomyType;
import org.jetbrains.annotations.NotNull;

public class HeadConfigModel {

    public EconomyType economy_type = EconomyType.ITEM;

    @SerializedName(value = "cost_item_type", alternate = "cost_type")
    public @NotNull String cost_item_type = "minecraft:emerald_block";

    @SerializedName(value = "cost_item_amount", alternate = "cost_amount")
    public int cost_item_amount = 1;
}

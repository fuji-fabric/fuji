package io.github.sakurawald.fuji.module.initializer.economy.config.model;

import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.module.initializer.economy.config.structure.CustomEconomyCurrencyDescriptor;
import java.util.ArrayList;
import java.util.List;

public class EconomyConfigModel {

    public String provider_icon = "minecraft:cherry_sapling";

    @Document(id = 1751826966542L, value = "Define your `custom economy currency` types.")
    public List<CustomEconomyCurrencyDescriptor> currencies = new ArrayList<>() {
        {
            this.add(CustomEconomyCurrencyDescriptor.make("fuji:gold","<gold>Fuji Gold" , "minecraft:gold_ingot", 100.0));
            this.add(CustomEconomyCurrencyDescriptor.make("fuji:diamond","<aqua>Fuji Diamond" , "minecraft:diamond", 0.0));
        }
    };

}

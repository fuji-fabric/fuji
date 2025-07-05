package io.github.sakurawald.fuji.module.initializer.economy.config.model;

import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.module.initializer.economy.structure.EconomyCurrencyDescriptor;
import java.util.ArrayList;
import java.util.List;

public class EconomyConfigModel {

    @Document("Define your `custom economy currency` types.")
    public List<EconomyCurrencyDescriptor> currencies = new ArrayList<>() {
        {
            this.add(EconomyCurrencyDescriptor.make("fuji:gold","<gold>Fuji Gold" , "minecraft:gold_ingot", 100.0));
            this.add(EconomyCurrencyDescriptor.make("fuji:diamond","<aqua>Fuji Diamond" , "minecraft:diamond", 0.0));
        }
    };

}

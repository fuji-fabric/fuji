package mod.fuji.module.initializer.economy.config.model;

import mod.fuji.core.document.annotation.Document;
import mod.fuji.module.initializer.economy.config.structure.CustomEconomyCurrencyDescriptor;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EconomyConfigModel {

    String providerIcon = "minecraft:cherry_sapling";

    int balanceTopPageSize = 10;


    @Document(id = 1751826966542L, value = "Define your `custom economy currency` types.")
    public List<CustomEconomyCurrencyDescriptor> currencies = new ArrayList<>() {
        {
            this.add(CustomEconomyCurrencyDescriptor.make("fuji:gold","<gold>Fuji Gold" , "minecraft:gold_ingot", 100.0, "<yellow>$%.2f"));
            this.add(CustomEconomyCurrencyDescriptor.make("fuji:diamond","<aqua>Fuji Diamond" , "minecraft:diamond", 0.0,  "<aqua>$%.2f"));
        }
    };

}

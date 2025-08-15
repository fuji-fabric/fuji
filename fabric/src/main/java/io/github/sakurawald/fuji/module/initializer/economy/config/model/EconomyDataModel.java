package io.github.sakurawald.fuji.module.initializer.economy.config.model;

import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.module.initializer.economy.config.structure.CustomEconomyCurrencyNode;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class EconomyDataModel {

    @Document(id = 1751826945592L, value = "Saved `accounts` for each `currency`.")
    public CopyOnWriteArrayList<CustomEconomyCurrencyNode> currencies = new CopyOnWriteArrayList<>();

}

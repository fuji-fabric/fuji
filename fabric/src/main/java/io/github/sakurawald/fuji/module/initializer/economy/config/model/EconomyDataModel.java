package io.github.sakurawald.fuji.module.initializer.economy.config.model;

import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.module.initializer.economy.structure.EconomyCurrencyNode;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class EconomyDataModel {

    @Document("Saved `accounts` for each `currency`.")
    public List<EconomyCurrencyNode> currencies = new ArrayList<>();

}

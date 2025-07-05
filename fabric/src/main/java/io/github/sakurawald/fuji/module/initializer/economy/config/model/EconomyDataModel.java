package io.github.sakurawald.fuji.module.initializer.economy.config.model;

import io.github.sakurawald.fuji.module.initializer.economy.structure.EconomyCurrencyNode;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class EconomyDataModel {

    public List<EconomyCurrencyNode> currencies = new ArrayList<>();

}

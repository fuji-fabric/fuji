package io.github.sakurawald.fuji.module.initializer.economy.structure;

import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class EconomyCurrencyNode {

    public String currencyId;
    public List<EconomyAccountNode> accounts = new ArrayList<>();

    public static EconomyCurrencyNode make(@NotNull String currencyId) {
        EconomyCurrencyNode economyCurrencyNode = new EconomyCurrencyNode();
        economyCurrencyNode.currencyId = currencyId;
        return economyCurrencyNode;
    }

}

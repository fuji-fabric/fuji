package io.github.sakurawald.fuji.module.initializer.economy.structure;

import io.github.sakurawald.fuji.core.document.annotation.Document;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class EconomyCurrencyNode {

    @Document("The `ID` of this `currency` type.")
    public String currencyId;

    @Document("The saved `accounts` for this type of `currency`.")
    public List<EconomyAccountNode> accounts = new ArrayList<>();

    public static EconomyCurrencyNode make(@NotNull String currencyId) {
        EconomyCurrencyNode economyCurrencyNode = new EconomyCurrencyNode();
        economyCurrencyNode.currencyId = currencyId;
        return economyCurrencyNode;
    }

}

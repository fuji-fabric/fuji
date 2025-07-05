package io.github.sakurawald.fuji.module.initializer.economy.structure;

import io.github.sakurawald.fuji.core.document.annotation.Document;
import org.jetbrains.annotations.NotNull;

public class EconomyCurrencyDescriptor {

    @Document("The `custom currency` identifier.")
    public String currencyId;
    @Document("The `display name` of this currency.")
    public String currencyName;
    @Document("The `display item` of this currency.")
    public String currencyIconItem;

    @Document("The default balance for this currency. (In raw value format)")
    public double defaultFaceBalance;

    public static EconomyCurrencyDescriptor make(@NotNull String currencyId, @NotNull String currencyName, @NotNull String currencyIconItem, double defaultFaceBalance) {
        EconomyCurrencyDescriptor descriptor = new EconomyCurrencyDescriptor();
        descriptor.currencyId = currencyId;
        descriptor.currencyName = currencyName;
        descriptor.currencyIconItem = currencyIconItem;
        descriptor.defaultFaceBalance = defaultFaceBalance;
        return descriptor;
    }

}

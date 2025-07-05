package io.github.sakurawald.fuji.module.initializer.economy.config.structure;

import io.github.sakurawald.fuji.core.document.annotation.Document;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class CustomEconomyCurrencyDescriptor {

    @Document("The `custom currency` identifier. The `namespace` must be `fuji`.")
    public String currencyId;

    @Document("The `display name` of this currency.")
    public String currencyName;

    @Document("The `display item` of this currency.")
    public String currencyIconItem;

    @Document("The default balance for this currency. (In raw value format)")
    public double defaultFaceBalance;

    @Document("The `formatted string` of this currency.")
    public String formatValueString = "%.2f";

    @Document("The `formatted text` of this currency.")
    public String formatValueText = "<yellow>$%.2f";

    public static CustomEconomyCurrencyDescriptor make(@NotNull String currencyId, @NotNull String currencyName, @NotNull String currencyIconItem, double defaultFaceBalance) {
        CustomEconomyCurrencyDescriptor descriptor = new CustomEconomyCurrencyDescriptor();
        descriptor.currencyId = currencyId;
        descriptor.currencyName = currencyName;
        descriptor.currencyIconItem = currencyIconItem;
        descriptor.defaultFaceBalance = defaultFaceBalance;
        return descriptor;
    }

}

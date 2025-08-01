package io.github.sakurawald.fuji.module.initializer.economy.config.structure;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.RegistryHelper;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import lombok.Data;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

@Data
public class CustomEconomyCurrencyDescriptor {

    @Document(id = 1751826969290L, value = "The `custom currency` identifier. The `namespace` must be `fuji`.")
    public String currencyId;

    @Document(id = 1751826972515L, value = "The `display name` of this currency.")
    public String currencyName;

    @Document(id = 1751826974438L, value = "The `display item` of this currency.")
    public String currencyIconItem;

    @Document(id = 1751826976552L, value = "The default balance for this currency. (In raw value format)")
    public double defaultFaceBalance;

    @Document(id = 1751826978686L, value = "The `formatted string` of this currency.")
    public String formatValueString = "%.2f";

    @Document(id = 1751826981029L, value = "The `formatted text` of this currency.")
    public String formatValueText = "<yellow>$%.2f";

    public static CustomEconomyCurrencyDescriptor make(@NotNull String currencyId, @NotNull String currencyName, @NotNull String currencyIconItem, double defaultFaceBalance, String formatValueText) {
        CustomEconomyCurrencyDescriptor descriptor = new CustomEconomyCurrencyDescriptor();
        descriptor.currencyId = currencyId;
        descriptor.currencyName = currencyName;
        descriptor.currencyIconItem = currencyIconItem;
        descriptor.defaultFaceBalance = defaultFaceBalance;
        descriptor.formatValueText = formatValueText;
        return descriptor;
    }

    public Identifier toIdentifier() {
        return RegistryHelper.makeIdentifierOrThrow(this.currencyId);
    }
}

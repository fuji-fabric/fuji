package io.github.sakurawald.fuji.module.initializer.economy.structure;

import eu.pb4.common.economy.api.EconomyCurrency;
import eu.pb4.common.economy.api.EconomyProvider;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.ItemStackHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.RegistryHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.module.initializer.economy.config.structure.CustomEconomyCurrencyDescriptor;
import lombok.Data;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

@Data
public class CustomEconomyCurrency implements EconomyCurrency {

    public final @NotNull CustomEconomyCurrencyDescriptor currencyDescriptor;

    public CustomEconomyCurrency(@NotNull CustomEconomyCurrencyDescriptor currencyDescriptor) {
        this.currencyDescriptor = currencyDescriptor;
    }

    @Override
    public Text name() {
        return TextHelper.getTextByValue(null, currencyDescriptor.currencyName);
    }

    @Override
    public Identifier id() {
        return RegistryHelper.makeIdentifierOrThrow(currencyDescriptor.currencyId);
    }

    @Override
    public ItemStack icon() {
        return ItemStackHelper.getItem(this.currencyDescriptor.currencyIconItem).getDefaultStack();
    }

    @Override
    public String formatValue(long value, boolean precise) {
        String formatValueString = this.currencyDescriptor.formatValueString;
        double faceValue = value / CustomEconomyProvider.SUPPORTED_PRECISE_FACTOR;
        return String.format(formatValueString, faceValue);
    }

    @Override
    public Text formatValueText(long value, boolean precise) {
        String formatValueText = this.currencyDescriptor.formatValueText;
        double faceValue = value / CustomEconomyProvider.SUPPORTED_PRECISE_FACTOR;
        formatValueText = String.format(formatValueText, faceValue);

        return TextHelper.getTextByValue(null, formatValueText);
    }

    @Override
    public long parseValue(String value) throws NumberFormatException {
        return Math.round(Double.parseDouble(value) * CustomEconomyProvider.SUPPORTED_PRECISE_FACTOR);
    }

    @Override
    public EconomyProvider provider() {
        return CustomEconomyProvider.INSTANCE;
    }
}

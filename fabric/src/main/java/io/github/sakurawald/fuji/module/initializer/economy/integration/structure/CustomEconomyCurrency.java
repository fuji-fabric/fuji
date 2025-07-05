package io.github.sakurawald.fuji.module.initializer.economy.integration.structure;

import eu.pb4.common.economy.api.EconomyCurrency;
import eu.pb4.common.economy.api.EconomyProvider;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.RegistryHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.module.initializer.economy.structure.EconomyCurrencyDescriptor;
import lombok.Data;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

@Data
public class CustomEconomyCurrency implements EconomyCurrency {

    public final @NotNull EconomyCurrencyDescriptor currencyDescriptor;

    public CustomEconomyCurrency(@NotNull EconomyCurrencyDescriptor currencyDescriptor) {
        this.currencyDescriptor = currencyDescriptor;
    }

    @Override
    public Text name() {
        return TextHelper.getTextByValue(null, currencyDescriptor.currencyName);
    }

    @Override
    public Identifier id() {
        return RegistryHelper.makeIdentifier(currencyDescriptor.currencyId);
    }

    @Override
    public ItemStack icon() {
        return RegistryHelper.ofItem(this.currencyDescriptor.currencyIconItem).getDefaultStack();
    }

    @Override
    public String formatValue(long value, boolean precise) {
        String formatValueString = this.currencyDescriptor.formatValueString;
        double faceValue = value / CustomEconomyProvider.SUPPORTED_PRECISE_FACTOR;
        return String.format(formatValueString, faceValue);
    }

    @Override
    public Text formatValueText(long value, boolean precise) {
        String formatValueString = formatValue(value, precise);
        return TextHelper.getTextByValue(null, formatValueString);
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

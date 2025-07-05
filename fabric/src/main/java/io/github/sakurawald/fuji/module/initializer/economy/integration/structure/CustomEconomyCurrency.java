package io.github.sakurawald.fuji.module.initializer.economy.integration.structure;

import eu.pb4.common.economy.api.EconomyCurrency;
import eu.pb4.common.economy.api.EconomyProvider;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.RegistryHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.module.initializer.economy.structure.EconomyCurrencyDescriptor;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("ClassCanBeRecord")
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
    public String formatValue(long value, boolean precise) {
        double faceValue = value / CustomEconomyProvider.SUPPORTED_PRECISE_FACTOR;
        return String.format("%.2f",  faceValue);
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

package mod.fuji.module.initializer.economy.structure;

import eu.pb4.common.economy.api.EconomyCurrency;
import eu.pb4.common.economy.api.EconomyProvider;
import mod.fuji.core.auxiliary.minecraft.ItemStackHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.structure.IdentifierIR;
import mod.fuji.module.initializer.economy.config.structure.CustomEconomyCurrencyDescriptor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

@Data
@NoArgsConstructor
public class CustomEconomyCurrency implements EconomyCurrency {

    public @NotNull CustomEconomyCurrencyDescriptor currencyDescriptor;

    public CustomEconomyCurrency(@NotNull CustomEconomyCurrencyDescriptor currencyDescriptor) {
        this.currencyDescriptor = currencyDescriptor;
    }

    @Override
    public Component name() {
        return TextHelper.getTextByValue(null, currencyDescriptor.currencyName);
    }

    @Override
    public
    #if MC_VER < MC_1_21_11
    net.minecraft.resources.ResourceLocation
    #elif MC_VER >= MC_1_21_11
    net.minecraft.resources.Identifier
    #endif id() {
        return IdentifierIR.makeIdentifierOrThrow(currencyDescriptor.currencyId).getNativeValue();
    }

    @Override
    public ItemStack icon() {
        return ItemStackHelper.Parser.parseItemStack(this.currencyDescriptor.currencyIconItem);
    }

    @Override
    public String formatValue(long value, boolean precise) {
        String formatValueString = this.currencyDescriptor.formatValueString;
        double faceValue = value / CustomEconomyProvider.SUPPORTED_PRECISE_FACTOR;
        return String.format(formatValueString, faceValue);
    }

    @Override
    public Component formatValueText(long value, boolean precise) {
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

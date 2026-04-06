package mod.fuji.module.initializer.economy.structure;

import eu.pb4.common.economy.api.EconomyCurrency;
import eu.pb4.common.economy.api.EconomyProvider;
import java.math.BigDecimal;
import java.math.BigInteger;
import mod.fuji.core.auxiliary.minecraft.ItemStackHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.structure.IdentifierIR;
import mod.fuji.module.initializer.economy.config.structure.CustomEconomyCurrencyDescriptor;
import lombok.Data;
import lombok.NoArgsConstructor;
import mod.fuji.module.initializer.economy.service.EconomyService;
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
    public String formatValue(#if MC_VER < MC_26_1 long #elif MC_VER >= MC_26_1 BigInteger #endif value, boolean precise) {
        String formatValueString = this.currencyDescriptor.formatValueString;

        // NOTE: Always treat the precise argument as true.
        BigInteger rawValue = EconomyService.toBigInteger(value);
        BigDecimal faceValue = EconomyService.toFaceValue(rawValue);
        return String.format(formatValueString, faceValue);
    }

    @Override
    public Component
    #if MC_VER < MC_26_1
    formatValueText
    #elif MC_VER >= MC_26_1
    formatValueComponent
    #endif
    (#if MC_VER < MC_26_1 long #elif MC_VER >= MC_26_1 BigInteger #endif value, boolean precise) {
        String formatValueString = this.formatValue(value, precise);
        return TextHelper.getTextByValue(null, formatValueString);
    }

    @Override
    public
    #if MC_VER < MC_26_1
    long
    #elif MC_VER >= MC_26_1
    BigInteger
    #endif
    parseValue(String value) throws NumberFormatException {
        double faceValue = Double.parseDouble(value);
        return EconomyService.toBalanceType(EconomyService.toRawValue(faceValue));
    }

    @Override
    public EconomyProvider provider() {
        return CustomEconomyProvider.INSTANCE;
    }
}

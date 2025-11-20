package mod.fuji.module.initializer.economy.structure;

import com.mojang.authlib.GameProfile;
import eu.pb4.common.economy.api.EconomyAccount;
import eu.pb4.common.economy.api.EconomyCurrency;
import eu.pb4.common.economy.api.EconomyProvider;
import eu.pb4.common.economy.api.EconomyTransaction;
import mod.fuji.core.auxiliary.minecraft.AuthlibHelper;
import mod.fuji.core.auxiliary.minecraft.RegistryHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.module.initializer.economy.EconomyInitializer;
import mod.fuji.module.initializer.economy.service.EconomyService;
import mod.fuji.module.initializer.economy.config.structure.CustomEconomyAccountNode;
import mod.fuji.module.initializer.economy.config.structure.CustomEconomyCurrencyDescriptor;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CustomEconomyAccount implements EconomyAccount {

    private final @Nullable GameProfile gameProfile;
    private final @NotNull CustomEconomyCurrencyDescriptor currencyDescriptor;

    public CustomEconomyAccount(@NotNull GameProfile gameProfile, @NotNull CustomEconomyCurrencyDescriptor currencyDescriptor) {
        this.gameProfile = gameProfile;
        this.currencyDescriptor = currencyDescriptor;
    }

    @Override
    public Component name() {
        return TextHelper.getTextByValue(this.gameProfile, this.currencyDescriptor.currencyName);
    }

    @Override
    public UUID owner() {
        if (this.gameProfile != null) {
            return AuthlibHelper.getId(this.gameProfile);
        }

        // NOTE: This is a server/console account.
        return Util.NIL_UUID;
    }

    @Override
    public ResourceLocation id() {
        // NOTE: Make the `account ID` identical to `currency ID`, for simplicity.
        return RegistryHelper.makeIdentifierOrThrow(this.currencyDescriptor.currencyId);
    }

    @Override
    public long balance() {
        CustomEconomyAccountNode accountNode = EconomyService.getCustomAccountNode(this.gameProfile, this.currencyDescriptor.toIdentifier());
        return accountNode.balance;
    }

    @Override
    public void setBalance(long value) {
        CustomEconomyAccountNode accountNode = EconomyService.getCustomAccountNode(this.gameProfile, this.currencyDescriptor.toIdentifier());
        accountNode.balance = value;
        EconomyInitializer.data.writeStorage();
    }

    @Override
    public EconomyTransaction canIncreaseBalance(long value) {
        return makeEconomyTransaction(value);
    }

    @Override
    public EconomyTransaction canDecreaseBalance(long value) {
        return makeEconomyTransaction(-value);
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    private EconomyTransaction makeEconomyTransaction(long deltaValue) {
        /* Define variables. */
        long previousBalance = this.balance();
        long transactionAmount = deltaValue;
        long finalBalance = previousBalance + transactionAmount;

        /* Check bounds. */
        boolean isSuccessful;
        Component feedbackText;

        if (finalBalance < 0
            || (transactionAmount > 0 && previousBalance > Long.MAX_VALUE - transactionAmount)
            || (transactionAmount < 0 && previousBalance < Long.MIN_VALUE - transactionAmount)) {
            isSuccessful = false;
            finalBalance = previousBalance;
            feedbackText = TextHelper.getTextByKey(gameProfile, "operation.fail");
        } else {
            isSuccessful = true;
            feedbackText = TextHelper.getTextByKey(gameProfile, "operation.success");
        }

        /* Make the transaction result. */
        return new CustomEconomyTransaction(this, isSuccessful, feedbackText, previousBalance, transactionAmount, finalBalance);
    }

    @Override
    public EconomyProvider provider() {
        return CustomEconomyProvider.INSTANCE;
    }

    @Override
    public EconomyCurrency currency() {
        return CustomEconomyProvider.getCustomEconomyCurrency(this.currencyDescriptor.toIdentifier());
    }

}

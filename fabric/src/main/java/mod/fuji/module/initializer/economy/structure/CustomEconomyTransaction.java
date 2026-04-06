package mod.fuji.module.initializer.economy.structure;

import eu.pb4.common.economy.api.EconomyAccount;
import eu.pb4.common.economy.api.EconomyTransaction;
import java.math.BigInteger;
import mod.fuji.module.initializer.economy.service.EconomyService;
import net.minecraft.network.chat.Component;

@SuppressWarnings("ClassCanBeRecord")
public class CustomEconomyTransaction implements EconomyTransaction {

    private final EconomyAccount account;
    private final boolean isSuccessful;
    private final Component message;
    private final BigInteger previousBalance;
    private final BigInteger transactionAmount;
    private final BigInteger finalBalance;

    public CustomEconomyTransaction(EconomyAccount account, boolean isSuccessful, Component message, BigInteger previousBalance, BigInteger transactionAmount, BigInteger finalBalance) {
        this.account = account;
        this.isSuccessful = isSuccessful;
        this.message = message;
        this.previousBalance = previousBalance;
        this.transactionAmount = transactionAmount;
        this.finalBalance = finalBalance;
    }

    @Override
    public boolean isSuccessful() {
        return this.isSuccessful;
    }

    @Override
    public Component message() {
        return this.message;
    }

    @Override
    public
    #if MC_VER < MC_26_1
    long
    #elif MC_VER >= MC_26_1
    BigInteger
    #endif
    finalBalance() {
        return EconomyService.toBalanceType(this.finalBalance);
    }

    @Override
    public
    #if MC_VER < MC_26_1
    long
    #elif MC_VER >= MC_26_1
    BigInteger
    #endif
    previousBalance() {
        return EconomyService.toBalanceType(this.previousBalance);
    }

    @Override
    public
    #if MC_VER < MC_26_1
    long
    #elif MC_VER >= MC_26_1
    BigInteger
    #endif
    transactionAmount() {
        return EconomyService.toBalanceType(this.transactionAmount);
    }

    @Override
    public EconomyAccount account() {
        return this.account;
    }

}

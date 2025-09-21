package mod.fuji.module.initializer.economy.structure;

import eu.pb4.common.economy.api.EconomyAccount;
import eu.pb4.common.economy.api.EconomyTransaction;
import net.minecraft.text.Text;

@SuppressWarnings("ClassCanBeRecord")
public class CustomEconomyTransaction implements EconomyTransaction {

    private final EconomyAccount account;
    private final boolean isSuccessful;
    private final Text message;
    private final long previousBalance;
    private final long transactionAmount;
    private final long finalBalance;

    public CustomEconomyTransaction(EconomyAccount account, boolean isSuccessful, Text message, long previousBalance, long transactionAmount, long finalBalance) {
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
    public Text message() {
        return this.message;
    }

    @Override
    public long finalBalance() {
        return this.finalBalance;
    }

    @Override
    public long previousBalance() {
        return this.previousBalance;
    }

    @Override
    public long transactionAmount() {
        return this.transactionAmount;
    }

    @Override
    public EconomyAccount account() {
        return this.account;
    }

}

package io.github.sakurawald.fuji.module.initializer.economy.structure;

import com.mojang.authlib.GameProfile;
import eu.pb4.common.economy.api.EconomyAccount;
import lombok.Data;

@Data
public class GameProfileAndEconomyAccount {
    public final GameProfile gameProfile;
    public final EconomyAccount economyAccount;

    public long getEconomyBalance() {
        return this.economyAccount.balance();
    }
}

package mod.fuji.module.initializer.economy.structure;

import com.mojang.authlib.GameProfile;
import eu.pb4.common.economy.api.EconomyAccount;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameProfileAndEconomyAccount {
    public GameProfile gameProfile;
    public EconomyAccount economyAccount;

    public long getEconomyBalance() {
        return this.economyAccount.balance();
    }
}

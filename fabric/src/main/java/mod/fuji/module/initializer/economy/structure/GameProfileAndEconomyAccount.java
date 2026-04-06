package mod.fuji.module.initializer.economy.structure;

import com.mojang.authlib.GameProfile;
import eu.pb4.common.economy.api.EconomyAccount;
import java.math.BigInteger;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import mod.fuji.module.initializer.economy.service.EconomyService;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameProfileAndEconomyAccount {
    public GameProfile gameProfile;
    public EconomyAccount economyAccount;

    public BigInteger getEconomyBalance() {
        return EconomyService.toBigInteger(this.economyAccount.balance());
    }
}

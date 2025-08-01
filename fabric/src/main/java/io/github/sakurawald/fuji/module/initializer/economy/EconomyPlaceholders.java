package io.github.sakurawald.fuji.module.initializer.economy;

import com.mojang.authlib.GameProfile;
import eu.pb4.common.economy.api.EconomyAccount;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlaceholderHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.RegistryHelper;
import io.github.sakurawald.fuji.core.document.annotation.DocStringProvider;
import io.github.sakurawald.fuji.core.document.descriptor.PlaceholderDescriptor;
import io.github.sakurawald.fuji.module.initializer.economy.service.EconomyService;
import java.util.Optional;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class EconomyPlaceholders {

    @DocStringProvider(id = 1753668968954L, value = """
        Returns the `balance` of the specified `currency` for the player.

        The syntax is `%fuji:balance \\<currency-id\\>%`
        For example, the `%fuji:balance fuji:gold%` will return the `balance` of the `fuji:gold` currency.

        ◉ Escape the placeholder properly.
        1. `/send-message %player:name% Your balance is %fuji:balance fuji:gold%`
        2. `/run as console send-message %player:name% Your balance is %fuji:balance fuji:gold%`

        To prevent the placeholder being parsed by the `/run` command.
        You need to insert a `backslash` character in case `2.` before the placeholder.
        """)
    public static void registerBalancePlaceholder() {
        PlaceholderDescriptor descriptor = new PlaceholderDescriptor("balance", 1753668968954L);
        PlaceholderHelper.registerPlayerPlaceholder(descriptor, (player, args) -> {
            GameProfile gameProfile = player.getGameProfile();
            Optional<Identifier> currencyId = RegistryHelper.makeIdentifier(args);
            return currencyId
                .map($currencyId -> {
                    Optional<EconomyAccount> economyAccount = EconomyService.getUserAccount(gameProfile, $currencyId);
                    return economyAccount
                        .map(EconomyAccount::formattedBalance)
                        .orElseGet(() -> Text.literal("[ECONOMY-ACCOUNT-NOT-FOUND-FOR-THIS-USER]"));
                })
                .orElse(Text.literal("[INVALID-CURRENCY-ID]"));
        });
    }

}

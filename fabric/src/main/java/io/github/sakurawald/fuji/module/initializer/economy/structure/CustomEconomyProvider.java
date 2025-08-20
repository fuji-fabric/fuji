package io.github.sakurawald.fuji.module.initializer.economy.structure;

import com.mojang.authlib.GameProfile;
import eu.pb4.common.economy.api.CommonEconomy;
import eu.pb4.common.economy.api.EconomyAccount;
import eu.pb4.common.economy.api.EconomyCurrency;
import eu.pb4.common.economy.api.EconomyProvider;
import io.github.sakurawald.fuji.Fuji;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.ItemStackHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.RegistryHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.document.annotation.ForDeveloper;
import io.github.sakurawald.fuji.module.initializer.economy.EconomyInitializer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

@ForDeveloper("""
    Each mod should register its own economy provider, to provide new types of currencies.
    One economy provider, can provide many many types of currencies.
    For each player, one economy account, holds one economy currency type.

    """)
public class CustomEconomyProvider implements EconomyProvider {

    public static final String CUSTOM_ECONOMY_PROVIDER_NAMESPACE = Fuji.MOD_ID;
    public static CustomEconomyProvider INSTANCE = new CustomEconomyProvider();

    public static final double SUPPORTED_PRECISE_FACTOR = 100.0;
    public static Map<Identifier, CustomEconomyCurrency> CURRENCY_ID_2_CURRENCY = new HashMap<>();

    public static void initializeCustomEconomyProvider() {
        // no-op
    }

    static {
        // NOTE: The provider ID is a namespace, not an identifier.
        CommonEconomy.register(CUSTOM_ECONOMY_PROVIDER_NAMESPACE, INSTANCE);
        registerDefinedFujiCurrencies();
    }

    public static CustomEconomyCurrency getCustomEconomyCurrency(Identifier currencyId) {
        return CURRENCY_ID_2_CURRENCY.get(currencyId);
    }

    @Override
    public Text name() {
        return Text.literal("FUJI_ECONOMY_PROVIDER")
            .fillStyle(Style.EMPTY
                .withColor(TextHelper.PRIMARY_COLOR_INT));
    }

    @Override
    public ItemStack icon() {
        return ItemStackHelper.Parser.parseItemStack(EconomyInitializer.config.model().getProviderIcon());
    }

    private static void registerDefinedFujiCurrencies() {
        EconomyInitializer.config.model().currencies.forEach(descriptor -> {
            Identifier currencyId = RegistryHelper.makeIdentifierOrThrow(descriptor.currencyId);
            RegistryHelper.ensureIdentifierNamespaceIsFuji(currencyId);

            CustomEconomyCurrency customEconomyCurrency = new CustomEconomyCurrency(descriptor);
            LogUtil.debug("Register custom economy currency: currencyId = {}, currencyDescriptor = {}", currencyId, customEconomyCurrency);
            CURRENCY_ID_2_CURRENCY.put(currencyId, customEconomyCurrency);
        });
    }

    @Override
    public @Nullable EconomyAccount getAccount(MinecraftServer server, GameProfile gameProfile, String pathOfCurrencyId) {
        // NOTE: The getAccount() method only use the `path` component of `identifier` for `currency`.
        Optional<EconomyAccount> first =
            getAccounts(server, gameProfile)
                .stream()
                .filter(account -> {
                    String path = account.currency().id().getPath();
                    return path.equals(pathOfCurrencyId);
                })
                .findFirst();

        if (first.isEmpty()) {
            LogUtil.error("getAccount(): gameProfile = {}, pathOfCurrencyId = {}", gameProfile.getName(), pathOfCurrencyId);
            throw new IllegalArgumentException("Failed to get account for specified account ID.");
        }

        return first.get();
    }

    @Override
    public Collection<EconomyAccount> getAccounts(MinecraftServer server, GameProfile gameProfile) {
        return EconomyInitializer
            .config
            .model()
            .currencies
            .stream()
            .map(economyCurrencyDescriptor -> new CustomEconomyAccount(gameProfile, economyCurrencyDescriptor))
            .collect(Collectors.toList());
    }

    @Override
    public @Nullable EconomyCurrency getCurrency(MinecraftServer server, String pathOfCurrencyId) {
        // NOTE: The getCurrency() method only use the `path` component of `identifier` for `currency`.

        Optional<Map.Entry<Identifier, CustomEconomyCurrency>> currencyOpt =
            CURRENCY_ID_2_CURRENCY
                .entrySet()
                .stream()
                .filter((entry) -> {
                    String path = entry.getKey().getPath();
                    return path.equals(pathOfCurrencyId);
                })
                .findFirst();

        if (currencyOpt.isEmpty()) {
            throw new IllegalArgumentException("Failed to find custom currency in server: path of currency ID = " + pathOfCurrencyId);
        }

        return currencyOpt.get().getValue();
    }

    @Override
    public Collection<EconomyCurrency> getCurrencies(MinecraftServer server) {
        // NOTE: Create a new array list, to bypass the class casting in generic types.
        return new ArrayList<>(CURRENCY_ID_2_CURRENCY.values());
    }

    @Override
    public @Nullable String defaultAccount(MinecraftServer server, GameProfile gameProfile, EconomyCurrency currency) {
        // NOTE: Pass the path of currency ID.
        return currency.id().getPath();
    }

}

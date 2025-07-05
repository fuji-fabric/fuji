package io.github.sakurawald.fuji.module.initializer.economy.integration.structure;

import com.mojang.authlib.GameProfile;
import eu.pb4.common.economy.api.CommonEconomy;
import eu.pb4.common.economy.api.EconomyAccount;
import eu.pb4.common.economy.api.EconomyCurrency;
import eu.pb4.common.economy.api.EconomyProvider;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.document.annotation.ForDeveloper;
import io.github.sakurawald.fuji.module.initializer.economy.EconomyInitializer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

@ForDeveloper("""
    Each mod should register its own economy provider, to provide new types of currencies.
    One economy provider, can provide many many types of currencies.
    For each player, one economy account, hold one type of economy currency.

    """)
public class CustomEconomyProvider implements EconomyProvider {

    public static final double SUPPORTED_PRECISE_FACTOR = 100.0;
    public static CustomEconomyProvider INSTANCE = new CustomEconomyProvider();
    public static Map<String, CustomEconomyCurrency> CURRENCY_ID_2_CURRENCY = new HashMap<>();

    public static void initializeCustomEconomyProvider() {
        // no-op
    }

    static {
        CommonEconomy.register("fuji_economy_provider", INSTANCE);
        registerDefinedFujiCurrencies();
    }

    @Override
    public Text name() {
        return Text.literal("FUJI_ECONOMY_PROVIDER");
    }

    @Override
    public ItemStack icon() {
        return Items.CHERRY_SAPLING.getDefaultStack();
    }

    private static void registerDefinedFujiCurrencies() {
        EconomyInitializer.config.model().currencies.forEach(descriptor -> {
            String currencyId = descriptor.currencyId;
            CustomEconomyCurrency customEconomyCurrency = new CustomEconomyCurrency(descriptor);
            CURRENCY_ID_2_CURRENCY.put(currencyId, customEconomyCurrency);
        });
    }

    @Override
    public @Nullable EconomyAccount getAccount(MinecraftServer server, GameProfile profile, String accountId) {
        LogUtil.info("get account: profile = {}, accountId = {}", profile.getId(), accountId);
        return null;
    }

    @Override
    public Collection<EconomyAccount> getAccounts(MinecraftServer server, GameProfile profile) {
        LogUtil.info("Get Accounts: profile = {}", profile.getId());
        List<EconomyAccount> accounts = new ArrayList<>();

        EconomyInitializer
            .config
            .model()
            .currencies
            .forEach(economyCurrencyDescriptor -> {
                CustomEconomyAccount customEconomyAccount = new CustomEconomyAccount(profile, economyCurrencyDescriptor);
                accounts.add(customEconomyAccount);
            });

        return accounts;
    }

    @Override
    public @Nullable EconomyCurrency getCurrency(MinecraftServer server, String currencyId) {
        return CURRENCY_ID_2_CURRENCY.get(currencyId);
    }

    @Override
    public Collection<EconomyCurrency> getCurrencies(MinecraftServer server) {
        // NOTE: Create a new array list, to bypass the class casting in generic types.
        return new ArrayList<>(CURRENCY_ID_2_CURRENCY.values());
    }

    @Override
    public @Nullable String defaultAccount(MinecraftServer server, GameProfile profile, EconomyCurrency currency) {
        // What is this?
        return "FUJI default account";
    }

}

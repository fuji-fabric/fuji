package io.github.sakurawald.fuji.module.initializer.economy.service;

import com.mojang.authlib.GameProfile;
import eu.pb4.common.economy.api.CommonEconomy;
import eu.pb4.common.economy.api.EconomyAccount;
import eu.pb4.common.economy.api.EconomyCurrency;
import eu.pb4.common.economy.api.EconomyProvider;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.fuji.module.initializer.economy.EconomyInitializer;
import io.github.sakurawald.fuji.module.initializer.economy.config.structure.CustomEconomyAccountNode;
import io.github.sakurawald.fuji.module.initializer.economy.config.structure.CustomEconomyCurrencyNode;
import io.github.sakurawald.fuji.module.initializer.economy.structure.CustomEconomyCurrency;
import io.github.sakurawald.fuji.module.initializer.economy.structure.CustomEconomyProvider;
import java.util.Collection;
import java.util.Optional;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class EconomyService {

    public static Collection<EconomyCurrency> getServerCurrencies() {
        MinecraftServer server = ServerHelper.getServer();
        return CommonEconomy.getCurrencies(server);
    }

    public static Collection<Identifier> getServerCurrencyIds() {
        return getServerCurrencies()
            .stream()
            .map(EconomyCurrency::id)
            .toList();
    }

    public static boolean isCurrencyInstalledOnThisServer(Identifier currencyId) {
        return getServerCurrencyIds()
            .stream()
            .anyMatch(it -> it.equals(currencyId));
    }

    public static Collection<EconomyProvider> getProviders() {
        return CommonEconomy.providers();
    }

    public static @NotNull Collection<EconomyAccount> getUserAccounts(GameProfile gameProfile) {
        MinecraftServer server = ServerHelper.getServer();
        return CommonEconomy.getAccounts(server, gameProfile);
    }

    public static @NotNull EconomyAccount getUserAccount(GameProfile gameProfile, Identifier currencyId) {
        Optional<EconomyAccount> accountForThatCurrencyId =
            getUserAccounts(gameProfile)
                .stream()
                .filter(account -> {
                    Identifier id = account.currency().id();
                    return id.equals(currencyId);
                })
                .findFirst();

        if (accountForThatCurrencyId.isEmpty()) {
            throw new IllegalArgumentException("Player %s didn't have the account for currency ID %s."
                .formatted(gameProfile.getName(), currencyId));
        }

        return accountForThatCurrencyId.get();
    }

    public static CustomEconomyCurrencyNode getCustomCurrencyNode(Identifier currencyId) {
        Optional<CustomEconomyCurrencyNode> currencyNoteOpt = EconomyInitializer.data.model()
            .currencies
            .stream()
            .filter(currency -> currency.currencyId.equals(currencyId.toString()))
            .findFirst();

        /* Make a new currency node. */
        if (currencyNoteOpt.isEmpty()) {
            if (isCurrencyInstalledOnThisServer(currencyId)) {
                CustomEconomyCurrencyNode customEconomyCurrencyNode = CustomEconomyCurrencyNode.make(currencyId.toString());
                EconomyInitializer.data.model().currencies.add(customEconomyCurrencyNode);
                EconomyInitializer.data.writeStorage();
                return customEconomyCurrencyNode;
            }

            throw new IllegalArgumentException("There is no currency ID %s in data file.".formatted(currencyId));
        }

        return currencyNoteOpt.get();
    }

    public static CustomEconomyAccountNode getCustomAccountNode(GameProfile gameProfile, Identifier currencyId) {
        /* Find the account of the game profile for the currency. */
        CustomEconomyCurrencyNode currencyNode = getCustomCurrencyNode(currencyId);
        Optional<CustomEconomyAccountNode> accountNodeOpt = currencyNode
            .accounts
            .stream()
            .filter(it -> it.ownerName.equals(gameProfile.getName()))
            .findFirst();

        /* Make a new account node. */
        if (accountNodeOpt.isEmpty()) {
            CustomEconomyCurrency customEconomyCurrency = CustomEconomyProvider.getCustomEconomyCurrency(currencyId);
            long defaultBalance = (long) (customEconomyCurrency.currencyDescriptor.defaultFaceBalance * CustomEconomyProvider.SUPPORTED_PRECISE_FACTOR);

            CustomEconomyAccountNode customEconomyAccountNode = CustomEconomyAccountNode.make(gameProfile, defaultBalance);
            currencyNode.accounts.add(customEconomyAccountNode);
            EconomyInitializer.data.writeStorage();
            return customEconomyAccountNode;
        }

        return accountNodeOpt.get();
    }
}

package io.github.sakurawald.fuji.module.initializer.economy.service;

import com.mojang.authlib.GameProfile;
import eu.pb4.common.economy.api.CommonEconomy;
import eu.pb4.common.economy.api.EconomyAccount;
import eu.pb4.common.economy.api.EconomyCurrency;
import eu.pb4.common.economy.api.EconomyProvider;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.fuji.module.initializer.economy.EconomyInitializer;
import io.github.sakurawald.fuji.module.initializer.economy.structure.CustomEconomyCurrency;
import io.github.sakurawald.fuji.module.initializer.economy.structure.CustomEconomyProvider;
import io.github.sakurawald.fuji.module.initializer.economy.config.structure.CustomEconomyAccountNode;
import io.github.sakurawald.fuji.module.initializer.economy.config.structure.CustomEconomyCurrencyNode;
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

    public static boolean isCurrencyInstalledOnThisServer(String currencyId) {
        return getServerCurrencyIds()
            .stream()
            .anyMatch(it -> it.toString().equals(currencyId));
    }

    public static Collection<EconomyProvider> getProviders() {
        return CommonEconomy.providers();
    }

    public static @NotNull Collection<EconomyAccount> getUserAccounts(GameProfile gameProfile) {
        MinecraftServer server = ServerHelper.getServer();
        return CommonEconomy.getAccounts(server, gameProfile);
    }

    public static @NotNull EconomyAccount getUserAccount(GameProfile gameProfile, String currencyId) {
        Collection<EconomyAccount> accounts = getUserAccounts(gameProfile);

        Optional<EconomyAccount> accountForThatCurrencyId = accounts
            .stream()
            .filter(account -> account.currency().id().toString().equals(currencyId))
            .findFirst();

        if (accountForThatCurrencyId.isEmpty()) {
            throw new IllegalArgumentException("Player %s didn't have the account for currency ID %s."
                .formatted(gameProfile.getName(), currencyId));
        }

        return accountForThatCurrencyId.get();
    }

    public static CustomEconomyCurrencyNode getCustomCurrencyNode(String currencyId) {
        Optional<CustomEconomyCurrencyNode> currencyNoteOpt = EconomyInitializer.data.model()
            .currencies
            .stream()
            .filter(currency -> currency.currencyId.equals(currencyId))
            .findFirst();

        /* Check if currency node exists. */
        if (currencyNoteOpt.isEmpty()) {
            if (isCurrencyInstalledOnThisServer(currencyId)) {
                CustomEconomyCurrencyNode customEconomyCurrencyNode = CustomEconomyCurrencyNode.make(currencyId);
                EconomyInitializer.data.model().currencies.add(customEconomyCurrencyNode);
                EconomyInitializer.data.writeStorage();
                return customEconomyCurrencyNode;
            }

            throw new IllegalArgumentException("There is no currency ID %s in data file.".formatted(currencyId));
        }

        return currencyNoteOpt.get();
    }

    public static CustomEconomyAccountNode getCustomAccountNode(GameProfile gameProfile, String currencyId) {
        CustomEconomyCurrencyNode currencyNode = getCustomCurrencyNode(currencyId);
        Optional<CustomEconomyAccountNode> accountNodeOpt = currencyNode
            .accounts
            .stream()
            .filter(it -> it.ownerName.equals(gameProfile.getName()))
            .findFirst();

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

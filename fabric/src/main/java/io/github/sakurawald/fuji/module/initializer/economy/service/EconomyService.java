package io.github.sakurawald.fuji.module.initializer.economy.service;

import com.mojang.authlib.GameProfile;
import eu.pb4.common.economy.api.CommonEconomy;
import eu.pb4.common.economy.api.EconomyAccount;
import eu.pb4.common.economy.api.EconomyCurrency;
import eu.pb4.common.economy.api.EconomyProvider;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.OfflineGameProfile;
import io.github.sakurawald.fuji.core.command.exception.AbortCommandExecutionException;
import io.github.sakurawald.fuji.module.initializer.economy.EconomyInitializer;
import io.github.sakurawald.fuji.module.initializer.economy.config.structure.CustomEconomyAccountNode;
import io.github.sakurawald.fuji.module.initializer.economy.config.structure.CustomEconomyCurrencyNode;
import io.github.sakurawald.fuji.module.initializer.economy.structure.CustomEconomyCurrency;
import io.github.sakurawald.fuji.module.initializer.economy.structure.CustomEconomyProvider;
import io.github.sakurawald.fuji.module.initializer.economy.structure.GameProfileAndEconomyAccount;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
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

    public static @NotNull Optional<EconomyAccount> getUserAccount(GameProfile gameProfile, Identifier currencyId) {
        return getUserAccounts(gameProfile)
                .stream()
                .filter(account -> {
                    Identifier id = account.currency().id();
                    return id.equals(currencyId);
                })
                .findFirst();
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

    public static void transferCurrency(ServerPlayerEntity source, OfflineGameProfile player, Identifier currencyId, double amount) {
        long deltaValue = (long) (amount * CustomEconomyProvider.SUPPORTED_PRECISE_FACTOR);
        deltaValue = Math.max(0, deltaValue);

        EconomyAccount fromAccount = tryGetEconomyAccount(source.getCommandSource(), source.getGameProfile(), currencyId);
        EconomyAccount toAccount = tryGetEconomyAccount(source.getCommandSource(), player.getValue(), currencyId);

        long fromAccountPreviousBalance = fromAccount.balance();
        long toAccountPreviousBalance = toAccount.balance();

        if (fromAccount.canDecreaseBalance(deltaValue).isSuccessful()
            && toAccount.canIncreaseBalance(deltaValue).isSuccessful()) {

            try {
                fromAccount.decreaseBalance(deltaValue);
                toAccount.increaseBalance(deltaValue);

                TextHelper.sendTextByKey(source, "operation.success");
            } catch (Exception rollbackIfFailed) {
                LogUtil.error("Failed to transfer currency {} with amount {} from account {} to account {}", currencyId, amount, fromAccount, toAccount, rollbackIfFailed);
                fromAccount.setBalance(fromAccountPreviousBalance);
                toAccount.setBalance(toAccountPreviousBalance);
                TextHelper.sendTextByKey(source, "operation.fail");
            }

        } else {
            TextHelper.sendTextByKey(source, "operation.fail");
        }
    }

    public static int getBalanceTopPageSize() {
        return EconomyInitializer.config.model().getBalanceTopPageSize();
    }

    public static @NotNull List<GameProfileAndEconomyAccount> makeBalanceTopEntities(ServerPlayerEntity player, Identifier currencyId) {
        return ServerHelper
            .getOfflineGameProfiles()
            .stream()
            .map(gameProfile -> {
                EconomyAccount economyAccount = tryGetEconomyAccount(player.getCommandSource(), gameProfile, currencyId);
                return new GameProfileAndEconomyAccount(gameProfile, economyAccount);
            })
            .sorted(Comparator.comparing(GameProfileAndEconomyAccount::getEconomyBalance)
                .reversed())
            .toList();
    }

    public static EconomyAccount tryGetEconomyAccount(@NotNull ServerCommandSource source, @NotNull GameProfile gameProfile, @NotNull Identifier currencyId) {
        Optional<EconomyAccount> economyAccount = getUserAccount(gameProfile, currencyId);
        if (economyAccount.isEmpty()) {
            TextHelper.sendTextByKey(source, "economy.account.not_found", gameProfile.getName(), currencyId);
            throw new AbortCommandExecutionException();
        }

        return economyAccount.get();
    }
}

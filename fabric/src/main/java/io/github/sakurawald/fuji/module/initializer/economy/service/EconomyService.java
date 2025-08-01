package io.github.sakurawald.fuji.module.initializer.economy.service;

import com.mojang.authlib.GameProfile;
import eu.pb4.common.economy.api.CommonEconomy;
import eu.pb4.common.economy.api.EconomyAccount;
import eu.pb4.common.economy.api.EconomyCurrency;
import eu.pb4.common.economy.api.EconomyProvider;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
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
import java.util.function.Function;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class EconomyService {

    public static @NotNull Collection<EconomyCurrency> getServerCurrencies() {
        MinecraftServer server = ServerHelper.getServer();
        return CommonEconomy.getCurrencies(server);
    }

    public static @NotNull Collection<Identifier> getServerCurrencyIds() {
        return getServerCurrencies()
            .stream()
            .map(EconomyCurrency::id)
            .toList();
    }

    public static boolean isCurrencyInstalledOnThisServer(@NotNull Identifier currencyId) {
        return getServerCurrencyIds()
            .stream()
            .anyMatch(it -> it.equals(currencyId));
    }

    public static @NotNull Collection<EconomyProvider> getProviders() {
        return CommonEconomy.providers();
    }

    public static @NotNull Collection<EconomyAccount> getUserAccounts(@NotNull GameProfile gameProfile) {
        MinecraftServer server = ServerHelper.getServer();
        return CommonEconomy.getAccounts(server, gameProfile);
    }

    public static @NotNull Optional<EconomyAccount> getUserAccount(@NotNull GameProfile gameProfile, @NotNull Identifier currencyId) {
        return getUserAccounts(gameProfile)
                .stream()
                .filter(account -> {
                    Identifier id = account.currency().id();
                    return id.equals(currencyId);
                })
                .findFirst();
    }

    private static @NotNull <T> T withCustomEconomyCurrencyNode(@NotNull Identifier currencyId, @NotNull Function<CustomEconomyCurrencyNode, T> function) {
        Optional<CustomEconomyCurrencyNode> customEconomyCurrencyNode = EconomyInitializer.data.model()
            .currencies
            .stream()
            .filter(currency -> currency.currencyId.equals(currencyId.toString()))
            .findFirst();

        CustomEconomyCurrencyNode $customEconomyCurrencyNode = customEconomyCurrencyNode.orElseGet(() -> {
            if (isCurrencyInstalledOnThisServer(currencyId)) {
                CustomEconomyCurrencyNode newValue = CustomEconomyCurrencyNode.make(currencyId.toString());
                EconomyInitializer.data.model().currencies.add(newValue);
                EconomyInitializer.data.writeStorage();
                return newValue;
            }

            throw new IllegalArgumentException("The currency %s didn't installed in the server.".formatted(currencyId));
        });

        return function.apply($customEconomyCurrencyNode);
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    public static @NotNull CustomEconomyAccountNode getCustomAccountNode(@NotNull GameProfile gameProfile, @NotNull Identifier currencyId) {
        return withCustomEconomyCurrencyNode(currencyId, customEconomyCurrencyNode -> {
            /* Find the account of the game profile for the currency. */
            Optional<CustomEconomyAccountNode> customEconomyAccountNode = customEconomyCurrencyNode
                .accounts
                .stream()
                .filter(it -> it.ownerName.equals(gameProfile.getName()))
                .findFirst();

            /* Make a new account node. */
            CustomEconomyAccountNode $customEconomyAccountNode = customEconomyAccountNode.orElseGet(() -> {
                CustomEconomyCurrency customEconomyCurrency = CustomEconomyProvider.getCustomEconomyCurrency(currencyId);
                long defaultBalance = (long) (customEconomyCurrency.currencyDescriptor.defaultFaceBalance * CustomEconomyProvider.SUPPORTED_PRECISE_FACTOR);

                CustomEconomyAccountNode newValue = CustomEconomyAccountNode.make(gameProfile, defaultBalance);
                customEconomyCurrencyNode.accounts.add(newValue);
                EconomyInitializer.data.writeStorage();
                return newValue;
            });

            return $customEconomyAccountNode;
        });
    }

    public static void transferCurrency(@NotNull ServerPlayerEntity source, @NotNull OfflineGameProfile player, @NotNull Identifier currencyId, double amount) {
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

    public static @NotNull List<GameProfileAndEconomyAccount> makeBalanceTopEntities(@NotNull ServerPlayerEntity player, @NotNull Identifier currencyId) {
        return PlayerHelper
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

    public static @NotNull EconomyAccount tryGetEconomyAccount(@NotNull ServerCommandSource source, @NotNull GameProfile gameProfile, @NotNull Identifier currencyId) {
        Optional<EconomyAccount> economyAccount = getUserAccount(gameProfile, currencyId);
        if (economyAccount.isEmpty()) {
            TextHelper.sendTextByKey(source, "economy.account.not_found", gameProfile.getName(), currencyId);
            throw new AbortCommandExecutionException();
        }

        return economyAccount.get();
    }
}

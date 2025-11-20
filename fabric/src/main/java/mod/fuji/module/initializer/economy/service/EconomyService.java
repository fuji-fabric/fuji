package mod.fuji.module.initializer.economy.service;

import com.mojang.authlib.GameProfile;
import eu.pb4.common.economy.api.CommonEconomy;
import eu.pb4.common.economy.api.EconomyAccount;
import eu.pb4.common.economy.api.EconomyCurrency;
import eu.pb4.common.economy.api.EconomyProvider;
import mod.fuji.core.auxiliary.LogUtil;
import mod.fuji.core.auxiliary.minecraft.AuthlibHelper;
import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.auxiliary.minecraft.ServerHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.command.argument.wrapper.impl.OfflineGameProfile;
import mod.fuji.core.command.exception.AbortCommandExecutionException;
import mod.fuji.module.initializer.economy.EconomyInitializer;
import mod.fuji.module.initializer.economy.config.structure.CustomEconomyAccountNode;
import mod.fuji.module.initializer.economy.config.structure.CustomEconomyCurrencyNode;
import mod.fuji.module.initializer.economy.structure.CustomEconomyCurrency;
import mod.fuji.module.initializer.economy.structure.CustomEconomyProvider;
import mod.fuji.module.initializer.economy.structure.GameProfileAndEconomyAccount;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.server.MinecraftServer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class EconomyService {

    public static @NotNull Collection<EconomyCurrency> getServerCurrencies() {
        MinecraftServer server = ServerHelper.getServer();
        return CommonEconomy.getCurrencies(server);
    }

    public static @NotNull Collection<ResourceLocation> getServerCurrencyIds() {
        return getServerCurrencies()
            .stream()
            .map(EconomyCurrency::id)
            .toList();
    }

    public static boolean isCurrencyInstalledOnThisServer(@NotNull ResourceLocation currencyId) {
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

    public static @NotNull Optional<EconomyAccount> getUserAccount(@NotNull GameProfile gameProfile, @NotNull ResourceLocation currencyId) {
        return getUserAccounts(gameProfile)
                .stream()
                .filter(account -> {
                    ResourceLocation id = account.currency().id();
                    return id.equals(currencyId);
                })
                .findFirst();
    }

    private static @NotNull <T> T withCustomEconomyCurrencyNode(@NotNull ResourceLocation currencyId, @NotNull Function<CustomEconomyCurrencyNode, T> function) {
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
    public static @NotNull CustomEconomyAccountNode getCustomAccountNode(@NotNull GameProfile gameProfile, @NotNull ResourceLocation currencyId) {
        return withCustomEconomyCurrencyNode(currencyId, customEconomyCurrencyNode -> {
            /* Find the account of the game profile for the currency. */
            Optional<CustomEconomyAccountNode> customEconomyAccountNode = customEconomyCurrencyNode
                .accounts
                .stream()
                .filter(it -> it.ownerName.equals(AuthlibHelper.getName(gameProfile)))
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

    public static void transferCurrency(@NotNull ServerPlayer source, @NotNull OfflineGameProfile player, @NotNull ResourceLocation currencyId, double amount) {
        long deltaValue = (long) (amount * CustomEconomyProvider.SUPPORTED_PRECISE_FACTOR);
        deltaValue = Math.max(0, deltaValue);

        EconomyAccount fromAccount = tryGetEconomyAccount(source.createCommandSourceStack(), source.getGameProfile(), currencyId);
        EconomyAccount toAccount = tryGetEconomyAccount(source.createCommandSourceStack(), player.getValue(), currencyId);

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

    public static @NotNull List<GameProfileAndEconomyAccount> makeBalanceTopEntities(@NotNull ServerPlayer player, @NotNull ResourceLocation currencyId) {
        return PlayerHelper.Cache
            .getOfflineGameProfiles()
            .stream()
            .map(gameProfile -> {
                EconomyAccount economyAccount = tryGetEconomyAccount(player.createCommandSourceStack(), gameProfile, currencyId);
                return new GameProfileAndEconomyAccount(gameProfile, economyAccount);
            })
            .sorted(Comparator.comparing(GameProfileAndEconomyAccount::getEconomyBalance)
                .reversed())
            .toList();
    }

    public static @NotNull EconomyAccount tryGetEconomyAccount(@NotNull CommandSourceStack source, @NotNull GameProfile gameProfile, @NotNull ResourceLocation currencyId) {
        Optional<EconomyAccount> economyAccount = getUserAccount(gameProfile, currencyId);
        if (economyAccount.isEmpty()) {
            TextHelper.sendTextByKey(source, "economy.account.not_found", AuthlibHelper.getName(gameProfile), currencyId);
            throw new AbortCommandExecutionException();
        }

        return economyAccount.get();
    }
}

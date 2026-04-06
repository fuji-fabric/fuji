package mod.fuji.module.initializer.economy.service;

import com.mojang.authlib.GameProfile;
import eu.pb4.common.economy.api.CommonEconomy;
import eu.pb4.common.economy.api.EconomyAccount;
import eu.pb4.common.economy.api.EconomyCurrency;
import eu.pb4.common.economy.api.EconomyProvider;
import java.math.BigDecimal;
import java.math.BigInteger;
import mod.fuji.core.auxiliary.LogUtil;
import mod.fuji.core.auxiliary.minecraft.AuthlibHelper;
import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.auxiliary.minecraft.ServerHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.command.argument.wrapper.impl.OfflineGameProfile;
import mod.fuji.core.command.exception.AbortCommandExecutionException;
import mod.fuji.core.structure.IdentifierIR;
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
import org.jetbrains.annotations.NotNull;

public class EconomyService {

    private static final BigDecimal FACE_VALUE_TO_RAW_VALUE_FACTOR = BigDecimal.valueOf(100);

    public static @NotNull Collection<EconomyCurrency> getServerCurrencies() {
        MinecraftServer server = ServerHelper.getServer();
        return CommonEconomy.getCurrencies(server);
    }

    public static @NotNull Collection<IdentifierIR> getServerCurrencyIds() {
        return getServerCurrencies()
            .stream()
            .map(EconomyCurrency::id)
            .map(IdentifierIR::of)
            .toList();
    }

    public static boolean isCurrencyInstalledOnThisServer(@NotNull IdentifierIR currencyId) {
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

    public static @NotNull Optional<EconomyAccount> getUserAccount(@NotNull GameProfile gameProfile, @NotNull IdentifierIR currencyId) {
        return getUserAccounts(gameProfile)
                .stream()
                .filter(account -> {
                    IdentifierIR id = IdentifierIR.of(account.currency().id());
                    return id.equals(currencyId);
                })
                .findFirst();
    }

    private static @NotNull <T> T withCustomEconomyCurrencyNode(@NotNull IdentifierIR currencyId, @NotNull Function<CustomEconomyCurrencyNode, T> function) {
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
    public static @NotNull CustomEconomyAccountNode getCustomAccountNode(@NotNull GameProfile gameProfile, @NotNull IdentifierIR currencyId) {
        return withCustomEconomyCurrencyNode(currencyId, customEconomyCurrencyNode -> {
            /* Find the account of the game profile for the currency. */
            Optional<CustomEconomyAccountNode> customEconomyAccountNode = customEconomyCurrencyNode
                .accounts
                .stream()
                .filter(it -> it.ownerName.equals(AuthlibHelper.getGameProfileName(gameProfile)))
                .findFirst();

            /* Make a new account node. */
            CustomEconomyAccountNode $customEconomyAccountNode = customEconomyAccountNode.orElseGet(() -> {
                CustomEconomyCurrency customEconomyCurrency = CustomEconomyProvider.getCustomEconomyCurrency(currencyId);
                BigInteger defaultBalance = EconomyService.toRawValue(customEconomyCurrency.currencyDescriptor.defaultFaceBalance);

                CustomEconomyAccountNode newValue = CustomEconomyAccountNode.make(gameProfile, defaultBalance);
                customEconomyCurrencyNode.accounts.add(newValue);
                EconomyInitializer.data.writeStorage();
                return newValue;
            });

            return $customEconomyAccountNode;
        });
    }

    public static void payCurrency(@NotNull ServerPlayer source, @NotNull OfflineGameProfile player, @NotNull IdentifierIR currencyId, double amount) {
        amount = Math.max(0, amount);

        BigInteger deltaValue = EconomyService.toRawValue(amount);

        EconomyAccount fromAccount = tryGetEconomyAccount(source.createCommandSourceStack(), source.getGameProfile(), currencyId);
        EconomyAccount toAccount = tryGetEconomyAccount(source.createCommandSourceStack(), player.getValue(), currencyId);

        BigInteger fromAccountPreviousBalance = EconomyService.toBigInteger(fromAccount.balance());
        BigInteger toAccountPreviousBalance = EconomyService.toBigInteger(toAccount.balance());

        if (fromAccount.canDecreaseBalance(EconomyService.toBalanceType(deltaValue)).isSuccessful()
            && toAccount.canIncreaseBalance(EconomyService.toBalanceType(deltaValue)).isSuccessful()) {

            try {
                fromAccount.decreaseBalance(EconomyService.toBalanceType(deltaValue));
                toAccount.increaseBalance(EconomyService.toBalanceType(deltaValue));

                TextHelper.sendTextByKey(source, "operation.success");
            } catch (Exception rollbackIfFailed) {
                LogUtil.error("Failed to transfer currency {} with amount {} from account {} to account {}", currencyId, amount, fromAccount, toAccount, rollbackIfFailed);
                fromAccount.setBalance(EconomyService.toBalanceType(fromAccountPreviousBalance));
                toAccount.setBalance(EconomyService.toBalanceType(toAccountPreviousBalance));
                TextHelper.sendTextByKey(source, "operation.fail");
            }

        } else {
            TextHelper.sendTextByKey(source, "operation.fail");
        }
    }

    public static int getBalanceTopPageSize() {
        return EconomyInitializer.config.model().getBalanceTopPageSize();
    }

    public static @NotNull List<GameProfileAndEconomyAccount> makeBalanceTopEntities(@NotNull ServerPlayer player, @NotNull IdentifierIR currencyId) {
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

    public static @NotNull EconomyAccount tryGetEconomyAccount(@NotNull CommandSourceStack source, @NotNull GameProfile gameProfile, @NotNull IdentifierIR currencyId) {
        Optional<EconomyAccount> economyAccount = getUserAccount(gameProfile, currencyId);
        if (economyAccount.isEmpty()) {
            TextHelper.sendTextByKey(source, "economy.account.not_found", AuthlibHelper.getGameProfileName(gameProfile), currencyId);
            throw new AbortCommandExecutionException();
        }

        return economyAccount.get();
    }

    public static @NotNull BigInteger toBigInteger(long value) {
        return BigInteger.valueOf(value);
    }

    public static @NotNull BigInteger toBigInteger(@NotNull BigInteger value) {
        return value;
    }

    public static long negate(long value) {
        return -value;
    }

    public static @NotNull BigInteger negate(@NotNull BigInteger value) {
        return value.negate();
    }

    public static
    #if MC_VER < MC_26_1
    long
    #elif MC_VER >= MC_26_1
    BigInteger
    #endif
    toBalanceType(@NotNull BigInteger value) {
        #if MC_VER < MC_26_1
        return value.longValue();
        #elif MC_VER >= MC_26_1
        return value;
        #endif
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    public static @NotNull BigInteger toRawValue(double faceValue) {
        BigDecimal $faceValue = new BigDecimal(faceValue);
        BigInteger rawValue = $faceValue.multiply(FACE_VALUE_TO_RAW_VALUE_FACTOR).toBigInteger();
        return rawValue;
    }

    public static @NotNull BigInteger getRawValue(@NotNull EconomyAccount economyAccount) {
        return EconomyService.toBigInteger(economyAccount.balance());
    }

    public static @NotNull BigDecimal toFaceValue(@NotNull BigInteger rawValue) {
        return new BigDecimal(rawValue).divide(FACE_VALUE_TO_RAW_VALUE_FACTOR);
    }

}

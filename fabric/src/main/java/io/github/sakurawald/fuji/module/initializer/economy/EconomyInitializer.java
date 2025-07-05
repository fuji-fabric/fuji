package io.github.sakurawald.fuji.module.initializer.economy;

import eu.pb4.common.economy.api.EconomyAccount;
import eu.pb4.common.economy.api.EconomyCurrency;
import eu.pb4.common.economy.api.EconomyProvider;
import eu.pb4.common.economy.api.EconomyTransaction;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.OfflineGameProfile;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.economy.command.argument.wrapper.CurrencyId;
import io.github.sakurawald.fuji.module.initializer.economy.config.model.EconomyConfigModel;
import io.github.sakurawald.fuji.module.initializer.economy.config.model.EconomyDataModel;
import io.github.sakurawald.fuji.module.initializer.economy.service.EconomyService;
import io.github.sakurawald.fuji.module.initializer.economy.structure.CustomEconomyProvider;
import java.util.Collection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Document("""
    This module allows you to enable the `economy gameplay`.
    And define your `custom currency types`.

    One `player` can have many `accounts`.
    One `account` holds one type of `currency`.
    """)

@ColorBox(color = ColorBox.ColorBlockTypes.TIPS, value = """
    You can use this module with `Universal Shops` mod.
    """)
public class EconomyInitializer extends ModuleInitializer {

    public static BaseConfigurationHandler<EconomyConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, EconomyConfigModel.class);

    public static BaseConfigurationHandler<EconomyDataModel> data = new ObjectConfigurationHandler<>("economy-data.json", EconomyDataModel.class);

    static {
        CustomEconomyProvider.initializeCustomEconomyProvider();
    }

    @Document("""
        List all installed `economy providers`, and what `economy currencies` they provided.
        """)
    @CommandNode("economy providers")
    @CommandRequirement(level = 4)
    private static int $providers(@CommandSource ServerCommandSource source) {
        Collection<EconomyProvider> providers = EconomyService.getProviders();
        source.sendMessage(Text.literal("There are %d providers installed in this server.".formatted(providers.size())));

        providers.forEach(provider -> {
            MinecraftServer server = ServerHelper.getServer();
            source.sendMessage(Text.literal("- Provider Id: %s".formatted(provider.id())));
            source.sendMessage(Text.literal("- Provider name: %s".formatted(provider.name())));
            source.sendMessage(Text.literal("- Provider icon: %s".formatted(provider.icon())));

            Collection<EconomyCurrency> currencies = provider.getCurrencies(server);
            currencies.forEach(currency -> {
                source.sendMessage(Text.literal("-- Currency Id: %s".formatted(currency.id())));
                source.sendMessage(Text.literal("-- Currency Name: %s".formatted(currency.name())));
                source.sendMessage(Text.literal("-- Currency Icon: %s".formatted(currency.icon().getItem())));
                source.sendMessage(TextHelper.TEXT_EMPTY);
            });
        });

        return CommandHelper.Return.SUCCESS;
    }

    @Document("""
        List all `accounts` owned by the `player`.
        """)
    @CommandNode("economy accounts")
    @CommandRequirement(level = 4)
    private static int $accounts(@CommandSource ServerCommandSource source, OfflineGameProfile player) {
        Collection<EconomyAccount> accounts = EconomyService.getUserAccounts(player.getValue());
        accounts.forEach(account -> {
            printEconomyAccountInfo(source, account);
        });

        return CommandHelper.Return.SUCCESS;
    }

    private static void printEconomyAccountInfo(ServerCommandSource source, EconomyAccount account) {
        source.sendMessage(Text.literal("- Account Id: %s".formatted(account.id())));
        source.sendMessage(Text.literal("-- Account Name: %s".formatted(account.name())));
        source.sendMessage(Text.literal("-- Account Icon: %s".formatted(account.accountIcon().getItem())));
        source.sendMessage(Text.literal("-- Account Owner: %s".formatted(account.owner())));
        source.sendMessage(Text.literal("-- Account Balance: %s".formatted(account.formattedBalance())));
        source.sendMessage(Text.literal("-- Account Currency Id: %s".formatted(account.currency().id())));
    }

    @Document("""
        Get the `player`'s `account` for `currency ID`.
        """)
    @CommandNode("economy account")
    @CommandRequirement(level = 4)
    private static int $account(@CommandSource ServerCommandSource source, OfflineGameProfile player, CurrencyId currencyId) {
        Identifier $currencyId = currencyId.getValue();
        EconomyAccount economyAccount = EconomyService.getUserAccount(player.getValue(), $currencyId);
        printEconomyAccountInfo(source, economyAccount);

        return CommandHelper.Return.SUCCESS;
    }

    @Document("""
        Query your economy `accounts`.
        """)
    @CommandNode("economy balance")
    private static int $balance(@CommandSource ServerPlayerEntity player) {
        OfflineGameProfile offlineGameProfile = new OfflineGameProfile(player.getGameProfile());
        $accounts(player.getCommandSource(), offlineGameProfile);
        return CommandHelper.Return.SUCCESS;
    }

    @Document("Give `amount` to the player's `account` for `specified currency`.")
    @CommandNode("economy give")
    @CommandRequirement(level = 4)
    private static int $give(@CommandSource ServerCommandSource source, OfflineGameProfile player, CurrencyId currencyId, double amount) {
        Identifier $currencyId = currencyId.getValue();
        EconomyAccount economyAccount = EconomyService.getUserAccount(player.getValue(), $currencyId);
        long deltaValue = (long) (amount * CustomEconomyProvider.SUPPORTED_PRECISE_FACTOR);
        EconomyTransaction economyTransaction = economyAccount.increaseBalance(deltaValue);

        source.sendMessage(economyTransaction.message());
        return CommandHelper.Return.SUCCESS;
    }

    @Document("Take `amount` from the player's `account` for `specified currency`.")
    @CommandNode("economy take")
    @CommandRequirement(level = 4)
    private static int $take(@CommandSource ServerCommandSource source, OfflineGameProfile player, CurrencyId currencyId, double amount) {
        Identifier $currencyId = currencyId.getValue();
        EconomyAccount economyAccount = EconomyService.getUserAccount(player.getValue(), $currencyId);
        long deltaValue = (long) (amount * CustomEconomyProvider.SUPPORTED_PRECISE_FACTOR);
        EconomyTransaction economyTransaction = economyAccount.decreaseBalance(deltaValue);

        source.sendMessage(economyTransaction.message());
        return CommandHelper.Return.SUCCESS;
    }

    @SuppressWarnings("SameParameterValue")
    @Document("Set the `amount` of the player's `account` for `specified currency`.")
    @CommandNode("economy set")
    @CommandRequirement(level = 4)
    private static int $set(@CommandSource ServerCommandSource source, OfflineGameProfile player, CurrencyId currencyId, double amount) {
        Identifier $currencyId = currencyId.getValue();
        EconomyAccount economyAccount = EconomyService.getUserAccount(player.getValue(), $currencyId);

        long finalValue = (long) (amount * CustomEconomyProvider.SUPPORTED_PRECISE_FACTOR);
        economyAccount.setBalance(finalValue);
        TextHelper.sendMessageByKey(source, "operation.success");
        return CommandHelper.Return.SUCCESS;
    }

    @Document("Clear the `amount` of the player's `account` for `specified currency`.")
    @CommandNode("economy clear")
    @CommandRequirement(level = 4)
    private static int $clear(@CommandSource ServerCommandSource source, OfflineGameProfile player, CurrencyId currencyId) {
        $set(source, player, currencyId, 0);
        return CommandHelper.Return.SUCCESS;
    }
}

package mod.fuji.module.initializer.economy;

import eu.pb4.common.economy.api.EconomyAccount;
import eu.pb4.common.economy.api.EconomyCurrency;
import eu.pb4.common.economy.api.EconomyProvider;
import eu.pb4.common.economy.api.EconomyTransaction;
import mod.fuji.core.auxiliary.minecraft.AuthlibHelper;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.ServerHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandRequirement;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.core.command.argument.wrapper.impl.OfflineGameProfile;
import mod.fuji.core.config.handler.abst.BaseConfigurationHandler;
import mod.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import mod.fuji.core.document.annotation.ColorBox;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.service.paged_text.PagedMessageText;
import mod.fuji.module.initializer.ModuleInitializer;
import mod.fuji.module.initializer.economy.command.argument.wrapper.CurrencyId;
import mod.fuji.module.initializer.economy.config.model.EconomyConfigModel;
import mod.fuji.module.initializer.economy.config.model.EconomyDataModel;
import mod.fuji.module.initializer.economy.gui.BalanceTopGui;
import mod.fuji.module.initializer.economy.service.EconomyService;
import mod.fuji.module.initializer.economy.structure.CustomEconomyProvider;
import mod.fuji.module.initializer.economy.structure.GameProfileAndEconomyAccount;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import net.minecraft.server.MinecraftServer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

@Document(id = 1751826915564L, value = """
    This module enables the `economy gameplay`.
    It allows defining `custom currency types`.

    A `player` can have multiple `accounts`.
    Each `account` holds one type of `currency`.
    """)
@ColorBox(id = 1751870587446L, color = ColorBox.ColorBoxTypes.TIP, value = """
    ◉ Make the `Admin Shops` and `Player Shops`.
    You can use this module with `Universal Shops` mod.
    This mod provides the `Admin Shops` and `Player Shops`.
    It brings the similar gameplay into `fabric` like `QuickShop` plugin in `bukkit`.
    """)
@ColorBox(id = 1751870591800L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ Create a `/balance` command for players to use.
    You can use `command_bundle` module.
    To create a `/balance` command, to wrap the `/economy account %player:name% fuji:gold` command.
    So the players can query the balance conveniently.

    ◉ Query the `balance` of all `currencies` for self.
    Issue: `/economy balance`

    ◉ Query the `balance` of all `currencies` for a target player.
    Issue: `/economy accounts Steve`

    ◉ Query the `balance` of the specified `currency` for a target player.
    Issue: `/economy account Steve fuji:gold`

    ◉ `Give`, `take`, `set` or `clear` the `balance` of specified `currency` for a player.
    1. `/economy give Steve fuji:gold 100`
    2. `/economy take Steve fuji:gold 100`
    3. `/economy set Steve fuji:gold 100`
    4. `/economy clear Steve fuji:gold --confirm true`

    ◉ Transfer the specified `balance` of specified `currency` from self to another player.
    Issue: `/economy pay Bob fuji:gold 100`
    """)
public class EconomyInitializer extends ModuleInitializer {

    public static BaseConfigurationHandler<EconomyConfigModel> config = ObjectConfigurationHandler.ofModule(BaseConfigurationHandler.CONFIG_JSON_LITERAL, EconomyConfigModel.class);

    public static BaseConfigurationHandler<EconomyDataModel> data = ObjectConfigurationHandler.ofModule("economy-data.json", EconomyDataModel.class);

    static {
        CustomEconomyProvider.initializeCustomEconomyProvider();
    }

    @Document(id = 1751826918349L, value = """
        List all installed `economy providers`, and what `economy currencies` they provided.
        """)
    @CommandNode("economy providers")
    @CommandRequirement(level = 4)
    private static int $providers(@CommandSource CommandSourceStack source) {
        Collection<EconomyProvider> providers = EconomyService.getProviders();
        TextHelper.sendMessageByText(source, Component.literal("There are %d providers installed in this server.".formatted(providers.size())));

        providers.forEach(provider -> {
            MinecraftServer server = ServerHelper.getServer();

            TextHelper.sendTextByKey(source, "line.separator");
            TextHelper.sendTextByKey(source, "economy.provider.id", provider.id());

            Component providerNameText = TextHelper.getTextByKey(source, "economy.provider.name.display");
            providerNameText = TextHelper.Replacer.replaceTextWithNamedArgument(providerNameText, "name", (matcher) -> provider.name());
            TextHelper.sendMessageByText(source, providerNameText);

            TextHelper.sendTextByKey(source, "economy.provider.icon", provider.icon().getItem());
            Collection<EconomyCurrency> currencies = provider.getCurrencies(server);
            currencies.forEach(currency -> {
                TextHelper.sendTextByKey(source, "economy.currency.id", currency.id());

                Component currencyNameText = TextHelper.getTextByKey(source, "economy.currency.name.display");
                currencyNameText = TextHelper.Replacer.replaceTextWithNamedArgument(currencyNameText, "name", (matcher) -> currency.name());
                TextHelper.sendMessageByText(source, currencyNameText);

                TextHelper.sendTextByKey(source, "economy.currency.icon", currency.icon().getItem());
                TextHelper.sendMessageByText(source, TextHelper.TEXT_EMPTY);
            });
        });

        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751826920505L, value = """
        List all `accounts` owned by the `player`.
        """)
    @CommandNode("economy accounts")
    @CommandRequirement(level = 4)
    private static int $accounts(@CommandSource CommandSourceStack source, OfflineGameProfile player) {
        Collection<EconomyAccount> accounts = EconomyService.getUserAccounts(player.getValue());
        accounts.forEach(account -> printEconomyAccountInfo(source, account));

        return CommandHelper.Return.SUCCESS;
    }

    private static void printEconomyAccountInfo(CommandSourceStack source, EconomyAccount account) {
        Component accountNameText = TextHelper.getTextByKey(source, "economy.account.name.display");
        accountNameText = TextHelper.Replacer.replaceTextWithNamedArgument(accountNameText, "name", (matcher) -> account.name());

        Component balanceText = TextHelper.getTextByKey(source, "economy.account.balance.display");
        balanceText = TextHelper.Replacer.replaceTextWithNamedArgument(balanceText, "balance", (matcher) -> account.formattedBalance());

        TextHelper.sendMessageByText(source, accountNameText);
        TextHelper.sendMessageByText(source, balanceText);
        TextHelper.sendMessageByText(source, TextHelper.TEXT_EMPTY);
    }

    @Document(id = 1751826922680L, value = """
        Get the `player`'s `account` for `currency ID`.
        """)
    @CommandNode("economy account")
    @CommandRequirement(level = 4)
    private static int $account(@CommandSource CommandSourceStack source, OfflineGameProfile player, CurrencyId currencyId) {
        ResourceLocation $currencyId = currencyId.getValue();
        EconomyAccount economyAccount = EconomyService.tryGetEconomyAccount(source, player.getValue(), $currencyId);
        printEconomyAccountInfo(source, economyAccount);

        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751826925194L, value = """
        Query your economy `accounts`.
        """)
    @CommandNode("economy balance")
    @CommandRequirement(level = 4)
    private static int $balance(@CommandSource ServerPlayer player) {
        OfflineGameProfile offlineGameProfile = new OfflineGameProfile(player.getGameProfile());
        $accounts(player.createCommandSourceStack(), offlineGameProfile);
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751826927389L, value = """
        List the top players of specified currency using GUI.
        """)
    @CommandNode("economy balance-top gui")
    @CommandRequirement(level = 4)
    private static int $balanceTopGui(@CommandSource ServerPlayer player, CurrencyId currencyId) {
        BalanceTopGui.make(player, currencyId.getValue())
            .open();
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1753498885843L, value = """
        List the top players of specified currency using message.
        """)
    @CommandNode("economy balance-top")
    @CommandRequirement(level = 4)
    private static int $balanceTop(@CommandSource ServerPlayer player, CurrencyId currencyId) {
        List<GameProfileAndEconomyAccount> entities = EconomyService.makeBalanceTopEntities(player, currencyId.getValue());
        PagedMessageText pagedMessageText = PagedMessageText.makePagedMessageText(player, entities, EconomyService.getBalanceTopPageSize(), (entity, index, pageBuilder) -> {
            int numbering = index + 1;
            String playerName = AuthlibHelper.getName(entity.getGameProfile());
            String balanceString = TextHelper.Operators.getString(entity.economyAccount.formattedBalance());
            pageBuilder.append(TextHelper.getTextByKey(player, "economy.balance.top.entry", numbering, playerName, balanceString));
        });
        pagedMessageText.sendPage(player, 0);
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751826931165L, value = "Give `amount` to the player's `account` for `specified currency`.")
    @CommandNode("economy give")
    @CommandRequirement(level = 4)
    private static int $give(@CommandSource CommandSourceStack source, OfflineGameProfile player, CurrencyId currencyId, double amount) {
        ResourceLocation $currencyId = currencyId.getValue();
        EconomyAccount economyAccount = EconomyService.tryGetEconomyAccount(source, player.getValue(), $currencyId);
        long deltaValue = (long) (amount * CustomEconomyProvider.SUPPORTED_PRECISE_FACTOR);
        EconomyTransaction economyTransaction = economyAccount.increaseBalance(deltaValue);

        TextHelper.sendMessageByText(source, economyTransaction.message());
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751826933066L, value = "Take `amount` from the player's `account` for `specified currency`.")
    @CommandNode("economy take")
    @CommandRequirement(level = 4)
    private static int $take(@CommandSource CommandSourceStack source, OfflineGameProfile player, CurrencyId currencyId, double amount) {
        ResourceLocation $currencyId = currencyId.getValue();
        EconomyAccount economyAccount = EconomyService.tryGetEconomyAccount(source, player.getValue(), $currencyId);
        long deltaValue = (long) (amount * CustomEconomyProvider.SUPPORTED_PRECISE_FACTOR);
        EconomyTransaction economyTransaction = economyAccount.decreaseBalance(deltaValue);

        TextHelper.sendMessageByText(source, economyTransaction.message());
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751826934778L, value = "Pay specified `amount` of `currency` to another player's account.")
    @CommandNode("economy pay")
    @CommandRequirement(level = 4)
    private static int $pay(@CommandSource ServerPlayer source, OfflineGameProfile player, CurrencyId currencyId, double amount) {
        EconomyService.transferCurrency(source, player, currencyId.getValue(), amount);
        return CommandHelper.Return.SUCCESS;
    }

    @SuppressWarnings("SameParameterValue")
    @Document(id = 1751826937120L, value = "Has the specified amount of currency?")
    @CommandNode("has-currency?")
    @CommandRequirement(level = 4)
    private static int $hasCurrency(@CommandSource CommandSourceStack source, OfflineGameProfile player, CurrencyId currencyId, double amount) {
        ResourceLocation $currencyId = currencyId.getValue();
        EconomyAccount economyAccount = EconomyService.tryGetEconomyAccount(source, player.getValue(), $currencyId);
        long finalValue = (long) (amount * CustomEconomyProvider.SUPPORTED_PRECISE_FACTOR);

        boolean value = economyAccount.balance() >= finalValue;
        return CommandHelper.Return.returnBoolean(source, value);
    }

    @SuppressWarnings("SameParameterValue")
    @Document(id = 1751826939422L, value = "Set the `amount` of the player's `account` for `specified currency`.")
    @CommandNode("economy set")
    @CommandRequirement(level = 4)
    private static int $set(@CommandSource CommandSourceStack source, OfflineGameProfile player, CurrencyId currencyId, double amount) {
        ResourceLocation $currencyId = currencyId.getValue();
        EconomyAccount economyAccount = EconomyService.tryGetEconomyAccount(source, player.getValue(), $currencyId);

        long finalValue = (long) (amount * CustomEconomyProvider.SUPPORTED_PRECISE_FACTOR);
        economyAccount.setBalance(finalValue);
        TextHelper.sendTextByKey(source, "operation.success");
        return CommandHelper.Return.SUCCESS;
    }


    @Document(id = 1751826942040L, value = "Clear the `amount` of the player's `account` for `specified currency`.")
    @CommandNode("economy clear")
    @CommandRequirement(level = 4)
    private static int $clear(@CommandSource CommandSourceStack source, OfflineGameProfile player, CurrencyId currencyId, Optional<Boolean> confirm) {
        return CommandHelper.Pattern.withCommandConfirmed(source, confirm, () -> {
            $set(source, player, currencyId, 0);
            return CommandHelper.Return.SUCCESS;
        });
    }

    @Override
    protected void registerPlaceholders() {
        EconomyPlaceholders.registerBalancePlaceholder();
    }
}

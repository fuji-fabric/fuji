package mod.fuji.module.initializer.economy.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import mod.fuji.core.auxiliary.minecraft.AuthlibHelper;
import mod.fuji.core.auxiliary.minecraft.GuiHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.gui.component.gui.PagedGui;
import mod.fuji.core.gui.structure.GuiElementIR;
import mod.fuji.core.structure.IdentifierIR;
import mod.fuji.module.initializer.economy.service.EconomyService;
import mod.fuji.module.initializer.economy.structure.GameProfileAndEconomyAccount;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BalanceTopGui extends PagedGui<GameProfileAndEconomyAccount> {

    private final IdentifierIR currencyId;

    public BalanceTopGui(@Nullable SimpleGui parent, @NotNull ServerPlayer player, IdentifierIR currencyId, @NotNull List<GameProfileAndEconomyAccount> entities, int pageIndex) {
        super(parent, player, TextHelper.getTextByKey(player, "economy.balance.top.gui.title", currencyId), entities, pageIndex);
        this.currencyId = currencyId;
    }

    @Override
    protected @NotNull PagedGui<GameProfileAndEconomyAccount> makePage(@Nullable SimpleGui parent, @NotNull ServerPlayer player, Component title, @NotNull List<GameProfileAndEconomyAccount> entities, int pageIndex) {
        return new BalanceTopGui(parent, player, this.currencyId, entities, pageIndex);
    }

    public static BalanceTopGui make(ServerPlayer player, IdentifierIR currencyId) {
        List<GameProfileAndEconomyAccount> entities = EconomyService.makeBalanceTopEntities(player, currencyId);
        return new BalanceTopGui(null, player, currencyId, entities, 0);
    }

    @Override
    protected @NotNull GuiElementIR toGuiElement(@NotNull GameProfileAndEconomyAccount entity) {
        List<Component> lore = new ArrayList<>();
        lore.add(TextHelper.getTextByKey(getPlayer(), "economy.balance", TextHelper.Operators.getString(entity.economyAccount.formattedBalance())));

        String name = AuthlibHelper.getGameProfileName(entity.gameProfile);
        GuiElementBuilder builder = GuiHelper.Button
            .makeLuckyBlockButton()
            .setName(Component.literal(name))
            .setLore(lore);

        return GuiElementIR.of(builder.build());
    }

    @Override
    protected void drawPagedGui() {
        super.drawPagedGui();

        GuiHelper.PlayerSkull.fillPlayerHeadTextures(this);
    }

}

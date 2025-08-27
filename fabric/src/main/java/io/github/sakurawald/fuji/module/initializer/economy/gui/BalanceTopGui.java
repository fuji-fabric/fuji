package io.github.sakurawald.fuji.module.initializer.economy.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.GuiHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.gui.component.gui.PagedGui;
import io.github.sakurawald.fuji.module.initializer.economy.service.EconomyService;
import io.github.sakurawald.fuji.module.initializer.economy.structure.GameProfileAndEconomyAccount;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BalanceTopGui extends PagedGui<GameProfileAndEconomyAccount> {

    private final Identifier currencyId;

    public BalanceTopGui(@Nullable SimpleGui parent, @NotNull ServerPlayerEntity player, Identifier currencyId, @NotNull List<GameProfileAndEconomyAccount> entities, int pageIndex) {
        super(parent, player, TextHelper.getTextByKey(player, "economy.balance.top.gui.title", currencyId), entities, pageIndex);
        this.currencyId = currencyId;
    }

    @Override
    protected PagedGui<GameProfileAndEconomyAccount> make(@Nullable SimpleGui parent, @NotNull ServerPlayerEntity player, Text title, @NotNull List<GameProfileAndEconomyAccount> entities, int pageIndex) {
        return new BalanceTopGui(parent, player, this.currencyId, entities, pageIndex);
    }

    public static BalanceTopGui make(ServerPlayerEntity player, Identifier currencyId) {
        List<GameProfileAndEconomyAccount> entities = EconomyService.makeBalanceTopEntities(player, currencyId);
        return new BalanceTopGui(null, player, currencyId, entities, 0);
    }

    @Override
    protected @NotNull GuiElementInterface toGuiElement(@NotNull GameProfileAndEconomyAccount entity) {
        List<Text> lore = new ArrayList<>();
        lore.add(TextHelper.getTextByKey(getPlayer(), "economy.balance", TextHelper.Operators.getString(entity.economyAccount.formattedBalance())));

        GuiElementBuilder builder = GuiHelper.Button
            .makeLuckyBlockButton()
            .setName(Text.literal(entity.gameProfile.getName()))
            .setLore(lore);

        return builder.build();
    }

    @Override
    protected void drawPagedGui() {
        super.drawPagedGui();

        GuiHelper.PlayerHead.fetchPlayerHeadTextures(this);
    }

}

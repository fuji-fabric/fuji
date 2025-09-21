package mod.fuji.module.initializer.economy.config.structure;

import mod.fuji.core.document.annotation.Document;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@Data
@NoArgsConstructor
public class CustomEconomyCurrencyNode {

    @Document(id = 1751826953095L, value = "The `ID` of this `currency` type.")
    public String currencyId;

    @Document(id = 1751826955882L, value = "The saved `accounts` for this type of `currency`.")
    public List<CustomEconomyAccountNode> accounts = new ArrayList<>();

    public static CustomEconomyCurrencyNode make(@NotNull String currencyId) {
        CustomEconomyCurrencyNode customEconomyCurrencyNode = new CustomEconomyCurrencyNode();
        customEconomyCurrencyNode.currencyId = currencyId;
        return customEconomyCurrencyNode;
    }

}

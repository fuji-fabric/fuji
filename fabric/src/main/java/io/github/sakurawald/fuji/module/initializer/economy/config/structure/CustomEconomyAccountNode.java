package io.github.sakurawald.fuji.module.initializer.economy.config.structure;

import com.mojang.authlib.GameProfile;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.AuthlibHelper;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CustomEconomyAccountNode {

    @Document(id = 1751826959911L, value = "The owner of this account.")
    public String ownerName;

    @Document(id = 1751826963753L, value = "The balance of this account.")
    public long balance;

    public static CustomEconomyAccountNode make(GameProfile gameProfile, long balance) {
        CustomEconomyAccountNode customEconomyAccountNode = new CustomEconomyAccountNode();
        customEconomyAccountNode.ownerName = AuthlibHelper.getName(gameProfile);
        customEconomyAccountNode.balance = balance;
        return customEconomyAccountNode;
    }

}


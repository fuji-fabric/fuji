package io.github.sakurawald.fuji.module.initializer.economy.config.structure;

import com.mojang.authlib.GameProfile;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import lombok.Data;

@Data
public class CustomEconomyAccountNode {

    @Document("The owner of this account.")
    public String ownerName;

    @Document("The balance of this account.")
    public long balance;

    public static CustomEconomyAccountNode make(GameProfile gameProfile, long balance) {
        CustomEconomyAccountNode customEconomyAccountNode = new CustomEconomyAccountNode();
        customEconomyAccountNode.ownerName = gameProfile.getName();
        customEconomyAccountNode.balance = balance;
        return customEconomyAccountNode;
    }

}


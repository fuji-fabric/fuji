package io.github.sakurawald.fuji.module.initializer.economy.structure;

import com.mojang.authlib.GameProfile;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import lombok.Data;

@Data
public class EconomyAccountNode {

    @Document("The owner of this account.")
    public String ownerName;

    @Document("The balance of this account.")
    public long balance;

    public static EconomyAccountNode make(GameProfile gameProfile, long balance) {
        EconomyAccountNode economyAccountNode = new EconomyAccountNode();
        economyAccountNode.ownerName = gameProfile.getName();
        economyAccountNode.balance = balance;
        return economyAccountNode;
    }

}


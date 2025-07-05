package io.github.sakurawald.fuji.module.initializer.economy.structure;

import com.mojang.authlib.GameProfile;

public class EconomyAccountNode {

    public String ownerName;
    public long balance;

    public static EconomyAccountNode make(GameProfile gameProfile, long balance) {
        EconomyAccountNode economyAccountNode = new EconomyAccountNode();
        economyAccountNode.ownerName = gameProfile.getName();
        economyAccountNode.balance = balance;
        return economyAccountNode;
    }

}


package io.github.sakurawald.fuji.module.initializer.tester;


import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.ItemStackHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.RegistryHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.tester.functions.TestFunctions;

import java.util.List;
import lombok.SneakyThrows;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.WorldBorderSizeChangedS2CPacket;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.world.GameRules;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.border.WorldBorderListener;

@Document(id = 1751980891153L, value = """
    This module is only used for `development`.
    If you are a developer, you can try new things here.
    You don't need to enable this module in production environment.
    It does not harm, but also not useful.
    """)
@CommandNode("tester")
@CommandRequirement(level = 4)
public class TesterInitializer extends ModuleInitializer {

    @SneakyThrows(Exception.class)
    @CommandNode("run")
    private static int $run(@CommandSource ServerCommandSource source) {

//        ItemStack mainHandStack = player.getMainHandStack();
//        NbtCompound nbt = ItemStackHelper.Nbt.getNbt(mainHandStack);
//        player.sendMessage(Text.literal(nbt.toString()));

        ServerWorld world = source.getWorld();
//
//        boolean keepInventory = world.getGameRules().getBoolean(GameRules.KEEP_INVENTORY);
//        player.sendMessage(Text.literal("keepInventory = " + keepInventory));
//
//        WorldBorder worldBorder = world.getWorldBorder();
//        player.sendMessage(Text.literal("worldBorder = " + worldBorder.getSize()));


//        player.networkHandler.sendPacket(new WorldBorderSizeChangedS2CPacket(worldBorder));
//
//        GameRules.RULE_TYPES
//            .entrySet()
//            .stream()
//                .forEach(entry -> {
//                    LogUtil.info("key = {}, value = {}", entry.getKey(), entry.getValue());
//
//                });

        LogUtil.info("Done");

        return 0;
    }

    @CommandNode("text-replace")
    private static int $testTextReplace(@CommandSource ServerPlayerEntity player) {
        TestFunctions.testTextReplacement(player);
        return 1;
    }

    @CommandNode("$1 minus $2")
    private static int $argumentReference(@CommandSource ServerPlayerEntity player, Integer a, Integer b) {
        player.sendMessage(Text.of(String.valueOf(a - b)));
        return 1;
    }

    @CommandNode("ctx")
    private static int $ctx(@CommandSource CommandContext<ServerCommandSource> ctx) {
        ctx.getSource().sendMessage(Text.of("root"));
        return 1;
    }

    @CommandNode
    private static int $root(@CommandSource ServerPlayerEntity player) {
        player.sendMessage(Text.of("root"));
        return 1;
    }
}

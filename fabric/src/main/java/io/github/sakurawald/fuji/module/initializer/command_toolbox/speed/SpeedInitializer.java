package io.github.sakurawald.fuji.module.initializer.command_toolbox.speed;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;


@Document(id = 1754399924653L, value = """
    Provides `/speed` command, to:
    1. Modify the `walk speed`.
    2. Modify the `fly speed`.
    """)
@ColorBox(id = 1754400656952L, color = ColorBox.ColorBlockTypes.EXAMPLE, value = """
    ◉ Double the `walk speed`
    The default value is `0.1`
    Issue: `/speed walk set Steve 0.2`

    ◉ Double the `fly speed`
    The default value is `0.05`
    Issue: `/speed fly set Steve 0.1`
    """)
public class SpeedInitializer extends ModuleInitializer {

    @CommandNode("speed walk set")
    @CommandRequirement(level = 4)
    private static int $setWalkSpeed(@CommandSource ServerCommandSource source, ServerPlayerEntity target, float speed) {
        target.getAbilities().setWalkSpeed(speed);
        target.sendAbilitiesUpdate();
        String targetName = PlayerHelper.getPlayerName(target);
        TextHelper.sendTextByKey(source, "speed.walk.set", targetName, speed);
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("speed fly set")
    @CommandRequirement(level = 4)
    private static int $setFlySpeed(@CommandSource ServerCommandSource source, ServerPlayerEntity target, float speed) {
        target.getAbilities().setFlySpeed(speed);
        target.sendAbilitiesUpdate();
        String targetName = PlayerHelper.getPlayerName(target);
        TextHelper.sendTextByKey(source, "speed.fly.set", targetName, speed);
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("speed walk get")
    @CommandRequirement(level = 4)
    private static int $getWalkSpeed(@CommandSource ServerCommandSource source, ServerPlayerEntity target) {
        float walkSpeed = target.getAbilities().getWalkSpeed();
        String targetName = PlayerHelper.getPlayerName(target);
        TextHelper.sendTextByKey(source, "speed.walk.get", targetName, walkSpeed);
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("speed fly get")
    @CommandRequirement(level = 4)
    private static int $getFlySpeed(@CommandSource ServerCommandSource source, ServerPlayerEntity target) {
        float flySpeed = target.getAbilities().getFlySpeed();
        String targetName = PlayerHelper.getPlayerName(target);
        TextHelper.sendTextByKey(source, "speed.fly.get", targetName, flySpeed);
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("speed walk reset")
    @CommandRequirement(level = 4)
    private static int $resetWalkSpeed(@CommandSource ServerCommandSource source, ServerPlayerEntity target) {
        $setWalkSpeed(source, target, 0.1F);
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("speed fly reset")
    @CommandRequirement(level = 4)
    private static int $resetFlySpeed(@CommandSource ServerCommandSource source, ServerPlayerEntity target) {
        $setFlySpeed(source, target, 0.05F);
        return CommandHelper.Return.SUCCESS;
    }
}

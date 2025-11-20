package mod.fuji.module.initializer.command_toolbox.speed;

import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandRequirement;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.core.document.annotation.ColorBox;
import mod.fuji.module.initializer.ModuleInitializer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;


@ColorBox(id = 1758036383834L, color = ColorBox.ColorBoxTypes.NOTE, value = """
    ◉ Re-join the server to fix the client-side camera broken.
    When a new `speed` is set, the client's `camera` will be broken.
    You have to re-join the server, to fix the client-side camera settings.
    """)
@ColorBox(id = 1754400656952L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
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
    private static int $setWalkSpeed(@CommandSource CommandSourceStack source, ServerPlayer target, float speed) {
        target.getAbilities().setWalkingSpeed(speed);
        target.onUpdateAbilities();
        String targetName = PlayerHelper.getPlayerName(target);
        TextHelper.sendTextByKey(source, "speed.walk.set", targetName, speed);
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("speed fly set")
    @CommandRequirement(level = 4)
    private static int $setFlySpeed(@CommandSource CommandSourceStack source, ServerPlayer target, float speed) {
        target.getAbilities().setFlyingSpeed(speed);
        target.onUpdateAbilities();
        String targetName = PlayerHelper.getPlayerName(target);
        TextHelper.sendTextByKey(source, "speed.fly.set", targetName, speed);
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("speed walk get")
    @CommandRequirement(level = 4)
    private static int $getWalkSpeed(@CommandSource CommandSourceStack source, ServerPlayer target) {
        float walkSpeed = target.getAbilities().getWalkingSpeed();
        String targetName = PlayerHelper.getPlayerName(target);
        TextHelper.sendTextByKey(source, "speed.walk.get", targetName, walkSpeed);
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("speed fly get")
    @CommandRequirement(level = 4)
    private static int $getFlySpeed(@CommandSource CommandSourceStack source, ServerPlayer target) {
        float flySpeed = target.getAbilities().getFlyingSpeed();
        String targetName = PlayerHelper.getPlayerName(target);
        TextHelper.sendTextByKey(source, "speed.fly.get", targetName, flySpeed);
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("speed walk reset")
    @CommandRequirement(level = 4)
    private static int $resetWalkSpeed(@CommandSource CommandSourceStack source, ServerPlayer target) {
        $setWalkSpeed(source, target, 0.1F);
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("speed fly reset")
    @CommandRequirement(level = 4)
    private static int $resetFlySpeed(@CommandSource CommandSourceStack source, ServerPlayer target) {
        $setFlySpeed(source, target, 0.05F);
        return CommandHelper.Return.SUCCESS;
    }
}

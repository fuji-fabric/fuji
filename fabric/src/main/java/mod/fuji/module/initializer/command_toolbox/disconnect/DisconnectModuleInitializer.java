package mod.fuji.module.initializer.command_toolbox.disconnect;

import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandRequirement;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.core.command.annotation.CommandTarget;
import mod.fuji.core.command.argument.wrapper.impl.GreedyString;
import mod.fuji.core.document.annotation.ColorBox;
import mod.fuji.module.initializer.ModuleInitializer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

@ColorBox(id = 1758033790822L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ Dis-connect a player.
    - `/dis-connect Steve \\<red\\>Kicked.`
    - `/dis-connect Steve \\<red\\>First Line\\<newline\\>Second Line`
    """)
public class DisconnectModuleInitializer extends ModuleInitializer {

    @CommandNode("dis-connect")
    @CommandRequirement(level = 4)
    private static int $disconnect(@CommandSource CommandSourceStack source, @CommandTarget ServerPlayer target, GreedyString reason) {
        Component reasonText = TextHelper.getTextByValue(target, reason.getValue());
        PlayerHelper.disconnectPlayer(target, reasonText);
        return CommandHelper.Return.SUCCESS;
    }

}

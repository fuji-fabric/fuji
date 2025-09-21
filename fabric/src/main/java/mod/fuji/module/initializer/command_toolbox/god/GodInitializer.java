package mod.fuji.module.initializer.command_toolbox.god;

import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandRequirement;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.core.command.annotation.CommandTarget;
import mod.fuji.module.initializer.ModuleInitializer;
import net.minecraft.server.network.ServerPlayerEntity;


public class GodInitializer extends ModuleInitializer {

    @Document(id = 1751825142583L, value = "Toggle the invulnerable state.")
    @CommandNode("god")
    @CommandRequirement(level = 4)
    private static int $god(@CommandSource @CommandTarget ServerPlayerEntity player) {
        boolean flag = !player.getAbilities().invulnerable;
        return $god(player, flag);
    }

    @Document(id = 1756710502594L, value = "Set the invulnerable state.")
    @CommandNode("god")
    @CommandRequirement(level = 4)
    private static int $god(@CommandSource @CommandTarget ServerPlayerEntity player, boolean flag) {
        player.getAbilities().invulnerable = flag;
        player.sendAbilitiesUpdate();

        TextHelper.sendTextByKey(player, flag ? "god.on" : "god.off");
        return CommandHelper.Return.SUCCESS;
    }
}

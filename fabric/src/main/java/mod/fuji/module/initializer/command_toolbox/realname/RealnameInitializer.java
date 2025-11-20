package mod.fuji.module.initializer.command_toolbox.realname;

import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.module.initializer.ModuleInitializer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;


public class RealnameInitializer extends ModuleInitializer {


    @SuppressWarnings("UnnecessaryLocalVariable")
    @Document(id = 1751825098969L, value = "Query the nickname-realname mapping.")
    @CommandNode("realname")
    private static int $realname(@CommandSource CommandSourceStack source) {
        MutableComponent builder = Component.empty();

        for (ServerPlayer player : PlayerHelper.Lookup.getOnlinePlayers()) {
            Component displayName = player.getDisplayName();
            if (displayName == null) continue;

            Component nickname = displayName;
            Component realname = player.getName();

            builder.append(nickname)
                .append(Component.literal(" -> "))
                .append(realname)
                .append(Component.literal("\n"));
        }

        TextHelper.sendMessageByText(source, builder);
        return CommandHelper.Return.SUCCESS;
    }

}

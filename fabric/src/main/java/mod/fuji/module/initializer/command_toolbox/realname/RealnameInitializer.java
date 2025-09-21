package mod.fuji.module.initializer.command_toolbox.realname;

import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.module.initializer.ModuleInitializer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;


public class RealnameInitializer extends ModuleInitializer {


    @SuppressWarnings("UnnecessaryLocalVariable")
    @Document(id = 1751825098969L, value = "Query the nickname-realname mapping.")
    @CommandNode("realname")
    private static int $realname(@CommandSource ServerCommandSource source) {
        MutableText builder = Text.empty();

        for (ServerPlayerEntity player : PlayerHelper.Lookup.getOnlinePlayers()) {
            Text displayName = player.getDisplayName();
            if (displayName == null) continue;

            Text nickname = displayName;
            Text realname = player.getName();

            builder.append(nickname)
                .append(Text.literal(" -> "))
                .append(realname)
                .append(Text.literal("\n"));
        }

        TextHelper.sendMessageByText(source, builder);
        return CommandHelper.Return.SUCCESS;
    }

}

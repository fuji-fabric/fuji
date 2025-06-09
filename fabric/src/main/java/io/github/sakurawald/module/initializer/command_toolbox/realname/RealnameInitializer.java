package io.github.sakurawald.module.initializer.command_toolbox.realname;

import io.github.sakurawald.core.annotation.Document;
import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.core.command.annotation.CommandNode;
import io.github.sakurawald.core.command.annotation.CommandSource;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;


public class RealnameInitializer extends ModuleInitializer {


    @SuppressWarnings("UnnecessaryLocalVariable")
    @CommandNode("realname")
    @Document("Query the nickname-realname mapping.")
    private static int $realname(@CommandSource ServerCommandSource source) {
        MutableText builder = Text.empty();

        for (ServerPlayerEntity player : ServerHelper.getPlayers()) {
            Text displayName = player.getDisplayName();
            if (displayName == null) continue;

            Text from = displayName;
            Text to = player.getName();

            builder.append(from)
                .append(Text.literal(" -> "))
                .append(to)
                .append(Text.literal("\n"));
        }

        source.sendMessage(builder);
        return CommandHelper.Return.SUCCESS;
    }

}

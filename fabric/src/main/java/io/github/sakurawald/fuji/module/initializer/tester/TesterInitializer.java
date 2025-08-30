package io.github.sakurawald.fuji.module.initializer.tester;


import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.command.annotation.CommandTarget;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;

import lombok.SneakyThrows;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

@Document(id = 1751980891153L, value = """
    This module is only used for `development`.
    If you are a developer, you can try new things here.
    You don't need to enable this module in production environment.
    It does not harm, but also not useful.
    """)
@CommandNode("tester")
@CommandRequirement(level = 4)
public class TesterInitializer extends ModuleInitializer {

    @SneakyThrows
    @CommandNode("run")
    private static int $run(@CommandSource ServerCommandSource source) {


        return CommandHelper.Return.SUCCESS;
    }
































    @CommandNode("split")
    private static int $split(@CommandSource ServerCommandSource source, @CommandTarget ServerPlayerEntity target, String string) {
        TextHelper.sendBroadcastByText(Text.literal("Run split(): source = %s, target = %s, string = %s".formatted(source.getName(), PlayerHelper.getPlayerName(target), string)));
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("non-split")
    private static int $nonSplit(@CommandSource @CommandTarget ServerPlayerEntity source, String string) {
        TextHelper.sendBroadcastByText(Text.literal("Run non-split(): source = %s, string = %s".formatted(PlayerHelper.getPlayerName(source), string)));
        return CommandHelper.Return.SUCCESS;
    }

}

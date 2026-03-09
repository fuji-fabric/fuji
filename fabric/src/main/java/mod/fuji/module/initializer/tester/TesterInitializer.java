package mod.fuji.module.initializer.tester;


import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandRequirement;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.core.command.annotation.CommandTarget;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.module.initializer.ModuleInitializer;
import lombok.SneakyThrows;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

@Document(id = 1751980891153L, value = """
    This module is only used for `development`.
    If you are a developer, you can try new things here.
    You don't need to enable this module in production environment.
    It does not harm, but also not useful.
    """)
@CommandNode("tester")
@CommandRequirement(level = 4)
public class TesterInitializer extends ModuleInitializer {

    @SneakyThrows(Throwable.class)
    @CommandNode("run")
    private static int $run(@CommandSource ServerPlayer player) {



        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("split")
    private static int $split(@CommandSource CommandSourceStack source, @CommandTarget ServerPlayer target, String string) {
        TextHelper.sendBroadcastByText(Component.literal("Run split(): source = %s, target = %s, string = %s".formatted(source.getTextName(), PlayerHelper.getPlayerName(target), string)));
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("non-split")
    private static int $nonSplit(@CommandSource @CommandTarget ServerPlayer source, String string) {
        TextHelper.sendBroadcastByText(Component.literal("Run non-split(): source = %s, string = %s".formatted(PlayerHelper.getPlayerName(source), string)));
        return CommandHelper.Return.SUCCESS;
    }

}

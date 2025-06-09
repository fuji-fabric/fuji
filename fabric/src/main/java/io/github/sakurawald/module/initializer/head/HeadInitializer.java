package io.github.sakurawald.module.initializer.head;

import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.core.annotation.Cite;
import io.github.sakurawald.core.annotation.Document;
import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.core.command.annotation.CommandNode;
import io.github.sakurawald.core.command.annotation.CommandRequirement;
import io.github.sakurawald.core.command.annotation.CommandSource;
import io.github.sakurawald.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import io.github.sakurawald.module.initializer.head.config.model.HeadConfigModel;
import io.github.sakurawald.module.initializer.head.gui.HeadGui;
import io.github.sakurawald.module.initializer.head.privoder.HeadProvider;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

@Cite("https://github.com/PotatoPresident/HeadIndex")
@CommandNode("head")
public class HeadInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<HeadConfigModel> head = new ObjectConfigurationHandler<>("head.json", HeadConfigModel.class);

    @CommandNode("sync")
    @CommandRequirement(level = 4)
    @Document("Download the head database from the internet. (You need to delete the existing head database file.)")
    private static int $sync(@CommandSource CommandContext<ServerCommandSource> ctx) {
        HeadProvider.fetchData();
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode
    private static int $head(@CommandSource ServerPlayerEntity player) {
        return $gui(player);
    }

    @CommandNode("gui")
    private static int $gui(@CommandSource ServerPlayerEntity player) {
        new HeadGui(player).open();
        return CommandHelper.Return.SUCCESS;
    }
}

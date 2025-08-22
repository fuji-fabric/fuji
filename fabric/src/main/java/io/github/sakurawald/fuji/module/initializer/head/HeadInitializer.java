package io.github.sakurawald.fuji.module.initializer.head;

import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.fuji.core.auxiliary.ReflectionUtil;
import io.github.sakurawald.fuji.core.config.migrator.transformer.impl.MoveFileTransformer;
import io.github.sakurawald.fuji.core.document.annotation.Cite;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.document.annotation.TestCase;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.head.config.model.HeadConfigModel;
import io.github.sakurawald.fuji.module.initializer.head.gui.HeadGui;
import io.github.sakurawald.fuji.module.initializer.head.privoder.HeadProvider;
import java.nio.file.Path;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

@Cite("https://github.com/PotatoPresident/HeadIndex")
@Document(id = 1751826596329L, value = """
    This module allows player to buy decorative heads from a head shop.
    """)
@TestCase(action = "Buy a new head in `/head`.", targets = "See if the structure of skin is changed by Mojang.")

@CommandNode("head")
public class HeadInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<HeadConfigModel> config = ObjectConfigurationHandler
        .ofModule("config.json", HeadConfigModel.class)
        .installTransformer(() -> {
            Path moduleConfigPath = ReflectionUtil.computeModuleConfigPath(HeadInitializer.class);
            Path sourceFilePath = moduleConfigPath.resolve("head.json");
            Path destinationFilePath = moduleConfigPath.resolve("config.json");
            return new MoveFileTransformer(sourceFilePath, destinationFilePath);
        });

    @Document(id = 1751826598337L, value = "Download the head database from the internet. (You need to delete the existing head database file.)")
    @CommandNode("sync")
    @CommandRequirement(level = 4)
    private static int $sync(@CommandSource CommandContext<ServerCommandSource> ctx) {
        HeadProvider.syncCategories();
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode
    @Document(id = 1751826599924L, value = "See /head gui")
    private static int $head(@CommandSource ServerPlayerEntity player) {
        return $gui(player);
    }

    @Document(id = 1751826601972L, value = "Open the head shop GUI.")
    @CommandNode("gui")
    private static int $gui(@CommandSource ServerPlayerEntity player) {
        new HeadGui(player)
            .open();
        return CommandHelper.Return.SUCCESS;
    }
}

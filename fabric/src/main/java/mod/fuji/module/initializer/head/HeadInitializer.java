package mod.fuji.module.initializer.head;

import com.mojang.brigadier.context.CommandContext;
import mod.fuji.core.auxiliary.ReflectionUtil;
import mod.fuji.core.config.migrator.transformer.impl.MoveFileTransformer;
import mod.fuji.core.document.annotation.Cite;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandRequirement;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.core.config.handler.abst.BaseConfigurationHandler;
import mod.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import mod.fuji.core.document.annotation.TestCase;
import mod.fuji.module.initializer.ModuleInitializer;
import mod.fuji.module.initializer.head.config.model.HeadConfigModel;
import mod.fuji.module.initializer.head.gui.HeadGui;
import mod.fuji.module.initializer.head.privoder.HeadProvider;
import java.nio.file.Path;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

@Cite("https://github.com/PotatoPresident/HeadIndex")
@Document(id = 1751826596329L, value = """
    This module allows players to purchase decorative heads from a `head shop`.
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
    private static int $sync(@CommandSource CommandContext<CommandSourceStack> ctx) {
        HeadProvider.syncCategories();
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode
    @Document(id = 1751826599924L, value = "See /head gui")
    private static int $head(@CommandSource ServerPlayer player) {
        return $gui(player);
    }

    @Document(id = 1751826601972L, value = "Open the head shop GUI.")
    @CommandNode("gui")
    private static int $gui(@CommandSource ServerPlayer player) {
        new HeadGui(player)
            .open();
        return CommandHelper.Return.SUCCESS;
    }
}

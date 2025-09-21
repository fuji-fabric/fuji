package mod.fuji.module.initializer.functional.enchantment;

import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandRequirement;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.core.command.annotation.CommandTarget;
import mod.fuji.core.config.handler.abst.BaseConfigurationHandler;
import mod.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import mod.fuji.module.initializer.ModuleInitializer;
import mod.fuji.module.initializer.functional.enchantment.config.model.EnchantmentConfigModel;
import mod.fuji.module.initializer.functional.enchantment.gui.MyEnchantmentScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class EnchantmentInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<EnchantmentConfigModel> config = ObjectConfigurationHandler.ofModule(BaseConfigurationHandler.CONFIG_JSON_LITERAL, EnchantmentConfigModel.class);

    @CommandNode("enchantment")
    @CommandRequirement(level = 4)
    private static int $enchantment(@CommandSource @CommandTarget ServerPlayerEntity player) {
        player.openHandledScreen(new SimpleNamedScreenHandlerFactory((i, inventory, p) -> new MyEnchantmentScreenHandler(i, inventory, ScreenHandlerContext.create(PlayerHelper.getServerWorld(p), p.getBlockPos())) {
        }, Text.translatable("container.enchant")));
        return CommandHelper.Return.SUCCESS;
    }
}

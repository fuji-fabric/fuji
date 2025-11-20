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
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

public class EnchantmentInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<EnchantmentConfigModel> config = ObjectConfigurationHandler.ofModule(BaseConfigurationHandler.CONFIG_JSON_LITERAL, EnchantmentConfigModel.class);

    @CommandNode("enchantment")
    @CommandRequirement(level = 4)
    private static int $enchantment(@CommandSource @CommandTarget ServerPlayer player) {
        player.openMenu(new SimpleMenuProvider((i, inventory, p) -> new MyEnchantmentScreenHandler(i, inventory, ContainerLevelAccess.create(PlayerHelper.getServerWorld(p), p.blockPosition())) {
        }, Component.translatable("container.enchant")));
        return CommandHelper.Return.SUCCESS;
    }
}

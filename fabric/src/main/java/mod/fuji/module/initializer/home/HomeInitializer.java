package mod.fuji.module.initializer.home;

import com.google.common.collect.BiMap;
import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.command.annotation.CommandRequirement;
import mod.fuji.core.command.argument.wrapper.impl.OfflinePlayerName;
import mod.fuji.core.document.annotation.DocStringProvider;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.LuckpermsHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.core.config.handler.abst.BaseConfigurationHandler;
import mod.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import mod.fuji.core.structure.GlobalPos;
import mod.fuji.core.document.descriptor.MetaDescriptor;
import mod.fuji.module.initializer.ModuleInitializer;
import mod.fuji.module.initializer.home.command.argument.wrapper.HomeName;
import mod.fuji.module.initializer.home.config.model.HomeDataModel;
import mod.fuji.module.initializer.home.gui.ListHomesGui;
import mod.fuji.module.initializer.home.service.HomeService;
import net.minecraft.server.network.ServerPlayerEntity;
import java.util.Optional;

@Document(id = 1751827004970L, value = """
    This module allows players to define their homes.
    """)
public class HomeInitializer extends ModuleInitializer {

    @DocStringProvider(id = 1752000367398L, value = """
        The home amount limit for this player.
        """)
    private static final MetaDescriptor<Integer> MAX_HOME_AMOUNT_META = new MetaDescriptor<>("fuji.home.home_limit", Integer::valueOf, 1752000367398L);

    public static final BaseConfigurationHandler<HomeDataModel> data = ObjectConfigurationHandler
        .ofModule("home.json", HomeDataModel.class)
        .enableAutoSaveFeature();

    @CommandNode("home tp")
    private static int $tp(@CommandSource ServerPlayerEntity player, HomeName home) {
        HomeService.ensureHomeNameExisting(player, home);
        BiMap<String, GlobalPos> homes = HomeService.withHomeMap(player);
        String homeName = home.getValue();

        GlobalPos globalPos = homes.get(homeName);
        globalPos.teleport(player);
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("home unset")
    private static int $unset(@CommandSource ServerPlayerEntity player, HomeName home) {
        HomeService.ensureHomeNameExisting(player, home);
        String playerName = PlayerHelper.getPlayerName(player);
        String homeName = home.getValue();
        HomeService.removeHome(playerName, homeName);

        TextHelper.sendTextByKey(player, "home.unset.success", homeName);
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("home set")
    private static int $set(@CommandSource ServerPlayerEntity player, HomeName home, Optional<Boolean> override) {
        BiMap<String, GlobalPos> homes = HomeService.withHomeMap(player);

        String homeName = home.getValue();
        if (homes.containsKey(homeName)) {
            if (!override.orElse(false)) {
                TextHelper.sendTextByKey(player, "home.set.fail.need_override", home);
                return CommandHelper.Return.FAILURE;
            }
        }

        Optional<Integer> maxHomes = LuckpermsHelper.getMeta(player.getUuid(), MAX_HOME_AMOUNT_META);
        if (maxHomes.isPresent() && homes.size() >= maxHomes.get()) {
            TextHelper.sendTextByKey(player, "home.set.fail.limit");
            return CommandHelper.Return.FAILURE;
        }

        homes.put(homeName, GlobalPos.of(player));
        TextHelper.sendTextByKey(player, "home.set.success", home);
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("home rename")
    private static int $rename(@CommandSource ServerPlayerEntity player, HomeName oldName, String newName) {
        HomeService.ensureHomeNameExisting(player, oldName);
        String playerName = PlayerHelper.getPlayerName(player);

        return HomeService
            .findHome(playerName, newName)
            .map(it -> {
                TextHelper.sendTextByKey(player, "home.rename.fail.exists", newName);
                return CommandHelper.Return.FAILURE;
            })
            .orElseGet(() -> {
                String $oldName = oldName.getValue();
                HomeService.renameHome(playerName, $oldName, newName);
                TextHelper.sendTextByKey(player , "home.rename.success", $oldName, newName);
                return CommandHelper.Return.SUCCESS;
            });
    }

    @CommandNode("home list")
    private static int $list(@CommandSource ServerPlayerEntity player) {
        TextHelper.sendTextByKey(player, "home.list", HomeService.withHomeMap(player).keySet());
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("home gui")
    private static int $gui(@CommandSource ServerPlayerEntity player) {
        OfflinePlayerName target = new OfflinePlayerName(PlayerHelper.getPlayerName(player));
        return $gui(player, target);
    }

    @CommandNode("home gui")
    @CommandRequirement(level = 4)
    private static int $gui(@CommandSource ServerPlayerEntity player, OfflinePlayerName target) {
        ListHomesGui
            .make(player, target.getValue())
            .open();
        return CommandHelper.Return.SUCCESS;
    }

}

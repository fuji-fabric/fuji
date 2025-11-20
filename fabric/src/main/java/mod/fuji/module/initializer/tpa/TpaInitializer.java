package mod.fuji.module.initializer.tpa;

import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.core.config.handler.abst.BaseConfigurationHandler;
import mod.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.module.initializer.ModuleInitializer;
import mod.fuji.module.initializer.tpa.config.model.TpaConfigModel;
import mod.fuji.module.initializer.tpa.gui.TpaGui;
import mod.fuji.module.initializer.tpa.service.TpaService;
import mod.fuji.module.initializer.tpa.structure.ResponseStatus;
import net.minecraft.server.level.ServerPlayer;


@Document(id = 1751826540953L, value = "This module provides `/tpa` and `/tpahere` commands.")
public class TpaInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<TpaConfigModel> config = ObjectConfigurationHandler.ofModule(BaseConfigurationHandler.CONFIG_JSON_LITERAL, TpaConfigModel.class);

    @CommandNode("tpa")
    private static int $tpa(@CommandSource ServerPlayer player, ServerPlayer target) {
        return TpaService.doRequest(player, target, false);
    }

    @CommandNode("tpahere")
    private static int $tpahere(@CommandSource ServerPlayer player, ServerPlayer target) {
        return TpaService.doRequest(player, target, true);
    }

    @CommandNode("tpaaccept")
    private static int $tpaaccept(@CommandSource ServerPlayer player, ServerPlayer target) {
        return TpaService.doResponse(player, target, ResponseStatus.ACCEPT);
    }

    @CommandNode("tpaaccept all")
    private static int $tpaaccept(@CommandSource ServerPlayer player) {
        return TpaService.doResponseToAll(player, ResponseStatus.ACCEPT);
    }

    @CommandNode("tpadeny")
    private static int $tpadeny(@CommandSource ServerPlayer player, ServerPlayer target) {
        return TpaService.doResponse(player, target, ResponseStatus.DENY);
    }

    @CommandNode("tpadeny all")
    private static int $tpadeny(@CommandSource ServerPlayer player) {
        return TpaService.doResponseToAll(player, ResponseStatus.DENY);
    }

    @CommandNode("tpacancel")
    private static int $tpacancel(@CommandSource ServerPlayer player, ServerPlayer target) {
        return TpaService.doResponse(player, target, ResponseStatus.CANCEL);
    }

    @CommandNode("tpacancel all")
    private static int $tpacancel(@CommandSource ServerPlayer player) {
        return TpaService.doResponseToAll(player, ResponseStatus.CANCEL);
    }

    @CommandNode("tpa")
    private static int $root(@CommandSource ServerPlayer player) {
        return $gui(player);
    }

    @CommandNode("tpa gui")
    private static int $gui(@CommandSource ServerPlayer player) {
        TpaGui
            .make(player)
            .open();
        return CommandHelper.Return.SUCCESS;
    }

}

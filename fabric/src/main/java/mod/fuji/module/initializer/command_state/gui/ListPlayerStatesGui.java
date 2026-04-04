package mod.fuji.module.initializer.command_state.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import mod.fuji.core.auxiliary.ChronosUtil;
import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.gui.component.gui.PagedGui;
import mod.fuji.core.gui.structure.GuiElementIR;
import mod.fuji.core.service.cache.structure.Cache;
import mod.fuji.module.initializer.command_state.service.CommandStateService;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.item.Items;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ListPlayerStatesGui extends PagedGui<GuiElementInterface> {

    final ServerPlayer target;

    public ListPlayerStatesGui(@Nullable SimpleGui parent, @NotNull ServerPlayer source, @NotNull ServerPlayer target, @NotNull List<GuiElementInterface> entities, int pageIndex) {
        super(parent, source, TextHelper.getTextByKey(source, "command_state.info.gui.title", PlayerHelper.getPlayerName(target)), entities, pageIndex);
        this.target = target;
    }

    @Override
    protected @NotNull PagedGui<GuiElementInterface> makePage(@Nullable SimpleGui parent, @NotNull ServerPlayer source, Component title, @NotNull List<GuiElementInterface> entities, int pageIndex) {
        return new ListPlayerStatesGui(parent, source, this.target, entities, pageIndex);
    }

    public static @NotNull ListPlayerStatesGui make(@NotNull ServerPlayer source, @NotNull ServerPlayer target) {
        List<GuiElementInterface> entities = new ArrayList<>();

        CommandStateService.withPlayerStateMap(target, playerStates -> {
            ConcurrentHashMap<String, Cache<Boolean>> stateMap = playerStates.getStateMap();

            stateMap.forEach((stateId, stateCache) -> {
                GuiElementBuilder builder = new GuiElementBuilder();
                builder.setName(Component.literal(stateId));
                boolean isInState = stateCache.getValue();
                builder.setItem(isInState ? Items.GREEN_BANNER : Items.RED_BANNER);
                builder.setLore(List.of(
                    TextHelper.getTextByKey(source, "entity.value", isInState),
                    TextHelper.getTextByKey(source, "entity.updated_timestamp", ChronosUtil.Formatter.formatDate(stateCache.getUpdatedTimestamp()))
                ));

                entities.add(builder.build());
            });
        });

        return new ListPlayerStatesGui(null, source, target, entities, 0);
    }


    @Override
    protected @NotNull GuiElementIR toGuiElement(@NotNull GuiElementInterface entity) {
        return GuiElementIR.of(entity);
    }
}

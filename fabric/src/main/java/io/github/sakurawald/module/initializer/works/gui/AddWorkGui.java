package io.github.sakurawald.module.initializer.works.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.sakurawald.core.auxiliary.minecraft.GuiHelper;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.core.gui.InputSignGui;
import io.github.sakurawald.module.initializer.works.WorksInitializer;
import io.github.sakurawald.module.initializer.works.config.model.WorksDataModel;
import io.github.sakurawald.module.initializer.works.structure.work.impl.NonProductionWork;
import io.github.sakurawald.module.initializer.works.structure.work.impl.ProductionWork;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

public class AddWorkGui extends InputSignGui {

    public AddWorkGui(@NotNull ServerPlayerEntity player) {
        super(player, TextHelper.getTextByKey(player, "works.work.add.prompt.input.name"));
    }

    @Override
    public void onClose() {
        /* input name */
        String name = this.getLine(0).getString().trim();
        if (name.isBlank()) {
            TextHelper.sendActionBarByKey(player, "works.work.add.empty_name");
            return;
        }

        /* input type */
        SimpleGui selectWorkTypeGui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, player, false);
        selectWorkTypeGui.setTitle(TextHelper.getTextByKey(player, "works.work.add.select_work_type.title"));
        GuiHelper.fill(selectWorkTypeGui, GuiHelper.makeSlotPlaceholder().getItemStack());

        BaseConfigurationHandler<WorksDataModel> worksHandler = WorksInitializer.works;
        selectWorkTypeGui.setSlot(11, new GuiElementBuilder().setItem(Items.GUNPOWDER).setName(TextHelper.getTextByKey(player, "works.non_production_work.name")).setCallback(() -> {
            // add
            worksHandler.model().works.addFirst(new NonProductionWork(player, name));
            TextHelper.sendActionBarByKey(player, "works.work.add.done");
            TextHelper.sendBroadcastByKey("works.work.add.broadcast", player.getGameProfile().getName(), name);
            selectWorkTypeGui.close();
        }));

        selectWorkTypeGui.setSlot(15, new GuiElementBuilder().setItem(Items.REDSTONE).setName(TextHelper.getTextByKey(player, "works.production_work.name")).setCallback(() -> {
            // add
            ProductionWork work = new ProductionWork(player, name);
            worksHandler.model().works.addFirst(work);
            TextHelper.sendActionBarByKey(player, "works.work.add.done");
            TextHelper.sendBroadcastByKey("works.work.add.broadcast", player.getGameProfile().getName(), name);
            selectWorkTypeGui.close();

            // input sample distance
            work.openInputSampleDistanceGui(player);
        }));
        selectWorkTypeGui.open();
    }

}

package io.github.sakurawald.fuji.module.initializer.works.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.EntityHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.GuiHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.LogicHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.RegistryHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.gui.impl.gui.ConfirmSignGui;
import io.github.sakurawald.fuji.core.gui.impl.gui.InputSignGui;
import io.github.sakurawald.fuji.module.initializer.works.WorksInitializer;
import io.github.sakurawald.fuji.module.initializer.works.structure.work.abst.Work;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

public class WorkGeneralSettingsGui extends SimpleGui {

    public WorkGeneralSettingsGui(@NotNull SimpleGui parentGui, @NotNull ServerPlayerEntity player, @NotNull Work work) {
        super(ScreenHandlerType.GENERIC_9X1, player, false);

        this.setTitle(TextHelper.getTextByKey(player, "works.work.set.general_settings.title"));

        placeSetEntityNameButton(player, work);
        placeSetEntityIconButton(player, work);
        placeSetEntityPositionButton(player, work);
        placeSetEntityIntroductionButton(player, work);
        placeSetEntityDeleteButton(player, work);

        this.setSlot(8, GuiHelper.Button.makePreviousPageButton(player)
            .setCallback(parentGui::open)
        );
    }

    private void placeSetEntityDeleteButton(@NotNull ServerPlayerEntity player, @NotNull Work work) {
        this.addSlot(new GuiElementBuilder()
            .setItem(Items.BARRIER)
            .setName(TextHelper.getTextByKey(player, "works.work.set.target.delete"))
            .setCallback(() -> new ConfirmSignGui(player) {
                @Override
                public void onConfirm() {
                    WorksInitializer.works.model().works.remove(work);
                    TextHelper.sendTextByKey(player, "works.work.delete.done");
                }
            }.open())

        );
    }

    private void placeSetEntityIntroductionButton(@NotNull ServerPlayerEntity player, @NotNull Work work) {
        this.addSlot(new GuiElementBuilder()
            .setItem(Items.PAINTING)
            .setName(TextHelper.getTextByKey(player, "works.work.set.target.icon"))
            .setCallback(() -> {
                /* Verify. */
                ItemStack mainHandItem = player.getMainHandStack();
                if (mainHandItem.isEmpty()) {
                    TextHelper.sendTextByKey(player, "works.work.set.target.icon.no_item");
                    close();
                    return;
                }

                /* Primary. */
                work.icon = RegistryHelper.toString(mainHandItem);
                TextHelper.sendTextByKey(player, "works.work.set.done", work.icon);
                close();
            })
        );
    }

    private void placeSetEntityPositionButton(@NotNull ServerPlayerEntity player, @NotNull Work work) {
        this.addSlot(new GuiElementBuilder()
            .setItem(Items.END_PORTAL_FRAME)
            .setName(TextHelper.getTextByKey(player, "works.work.set.target.position"))
            .setCallback(() -> {
                work.level = EntityHelper.getServerWorld(player).getRegistryKey().getValue().toString();
                work.x = player.getPos().x;
                work.y = player.getPos().y;
                work.z = player.getPos().z;
                TextHelper.sendTextByKey(player, "works.work.set.done", "(%s, %f, %f, %f)".formatted(work.level, work.x, work.y, work.z));
                close();
            })
        );
    }

    private void placeSetEntityIconButton(@NotNull ServerPlayerEntity player, @NotNull Work work) {
        this.addSlot(new GuiElementBuilder()
            .setItem(Items.CHERRY_HANGING_SIGN)
            .setName(TextHelper.getTextByKey(player, "works.work.set.target.introduction"))
            .setCallback(() -> new InputSignGui(player, null) {
                @Override
                public void onClose() {
                    String newIntroduction = this.joinStrings();
                    LogicHelper.withCancelCheck(player, newIntroduction.isBlank(), () -> {
                        work.introduction = newIntroduction;
                        TextHelper.sendTextByKey(player, "works.work.set.done", work.introduction);
                    });
                }
            }.open())
        );
    }

    private void placeSetEntityNameButton(@NotNull ServerPlayerEntity player, @NotNull Work work) {
        this.addSlot(new GuiElementBuilder()
            .setItem(Items.NAME_TAG)
            .setName(TextHelper.getTextByKey(player, "works.work.set.target.name"))
            .setCallback(() -> new InputSignGui(player, null) {
                @Override
                public void onClose() {
                    String newValue = this.joinStrings();
                    if (newValue.isBlank()) {
                        TextHelper.sendTextByKey(player, "works.work.add.empty_name");
                        return;
                    }

                    work.name = newValue;
                    TextHelper.sendTextByKey(player, "works.work.set.done", work.name);
                }
            }.open())
        );
    }
}

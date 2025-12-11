package mod.fuji.module.initializer.works.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import mod.fuji.core.auxiliary.minecraft.EntityHelper;
import mod.fuji.core.auxiliary.minecraft.GuiHelper;
import mod.fuji.core.auxiliary.minecraft.LogicHelper;
import mod.fuji.core.auxiliary.minecraft.RegistryHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.gui.component.gui.ConfirmSignGui;
import mod.fuji.core.gui.component.gui.InputSignGui;
import mod.fuji.module.initializer.works.WorksInitializer;
import mod.fuji.module.initializer.works.structure.work.abst.Work;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public class WorkGeneralSettingsGui extends SimpleGui {

    public WorkGeneralSettingsGui(@NotNull SimpleGui parentGui, @NotNull ServerPlayer player, @NotNull Work work) {
        super(MenuType.GENERIC_9x1, player, false);

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

    private void placeSetEntityDeleteButton(@NotNull ServerPlayer player, @NotNull Work work) {
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

    private void placeSetEntityIntroductionButton(@NotNull ServerPlayer player, @NotNull Work work) {
        this.addSlot(new GuiElementBuilder()
            .setItem(Items.PAINTING)
            .setName(TextHelper.getTextByKey(player, "works.work.set.target.icon"))
            .setCallback(() -> {
                /* Verify. */
                ItemStack mainHandItem = player.getMainHandItem();
                if (mainHandItem.isEmpty()) {
                    TextHelper.sendTextByKey(player, "works.work.set.target.icon.no_item");
                    close();
                    return;
                }

                /* Primary. */
                work.icon = RegistryHelper.getIdAsString(mainHandItem);
                TextHelper.sendTextByKey(player, "works.work.set.done", work.icon);
                close();
            })
        );
    }

    private void placeSetEntityPositionButton(@NotNull ServerPlayer player, @NotNull Work work) {
        this.addSlot(new GuiElementBuilder()
            .setItem(Items.END_PORTAL_FRAME)
            .setName(TextHelper.getTextByKey(player, "works.work.set.target.position"))
            .setCallback(() -> {
                work.level = RegistryHelper
                    .getIdentifier(EntityHelper.getServerWorld(player).dimension())
                    .toString();
                work.x = EntityHelper.getPos(player).x;
                work.y = EntityHelper.getPos(player).y;
                work.z = EntityHelper.getPos(player).z;
                TextHelper.sendTextByKey(player, "works.work.set.done", "(%s, %f, %f, %f)".formatted(work.level, work.x, work.y, work.z));
                close();
            })
        );
    }

    private void placeSetEntityIconButton(@NotNull ServerPlayer player, @NotNull Work work) {
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

    private void placeSetEntityNameButton(@NotNull ServerPlayer player, @NotNull Work work) {
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

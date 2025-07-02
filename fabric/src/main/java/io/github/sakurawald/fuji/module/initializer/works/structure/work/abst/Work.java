package io.github.sakurawald.fuji.module.initializer.works.structure.work.abst;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.sakurawald.fuji.core.auxiliary.ChronosUtil;
import io.github.sakurawald.fuji.core.auxiliary.RandomUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.EntityHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.GuiHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.RegistryHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.gui.impl.gui.ConfirmSignGui;
import io.github.sakurawald.fuji.core.gui.impl.gui.InputSignGui;
import io.github.sakurawald.fuji.module.initializer.works.WorksInitializer;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Data
public abstract class Work {

    @Document("The type of this work.")
    public String type;

    @Document("The unique id of this work.")
    public String id;

    @Document("The create time of this work.")
    public long createTimeMS;

    @Document("Which player created this work.")
    public String creator;

    @Document("The display name of this work.")
    public String name;

    @Document("The introduction of this work.")
    public @Nullable String introduction;

    public String level;
    public double x;
    public double y;
    public double z;
    public float yaw;
    public float pitch;

    @Document("The display item of this work.")
    public @Nullable String icon;

    public Work(@NotNull ServerPlayerEntity player, String name) {
        this.type = getEntityType();
        this.id = RandomUtil.randomUUID();
        this.createTimeMS = System.currentTimeMillis();
        this.creator = PlayerHelper.getPlayerName(player);
        this.name = name;
        this.introduction = null;
        this.level = RegistryHelper.toString(player.getWorld());
        this.x = player.getPos().x;
        this.y = player.getPos().y;
        this.z = player.getPos().z;
        this.yaw = player.getYaw();
        this.pitch = player.getPitch();
        this.icon = null;
    }

    protected abstract String getEntityType();

    protected abstract Item getDefaultEntityIcon();

    public Item getEntityIcon() {
        if (this.icon == null) {
            return this.getDefaultEntityIcon();
        }

        return RegistryHelper.ofItem(this.icon);
    }

    public abstract void openSpecializedSettingsGui(ServerPlayerEntity player, SimpleGui parentGui);

    public void openGeneralSettingsGui(@NotNull ServerPlayerEntity player, @NotNull SimpleGui parentGui) {
        Work work = this;
        final SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X1, player, false);
        gui.setLockPlayerInventory(true);
        gui.setTitle(TextHelper.getTextByKey(player, "works.work.set.general_settings.title"));
        gui.addSlot(new GuiElementBuilder()
            .setItem(Items.NAME_TAG)
            .setName(TextHelper.getTextByKey(player, "works.work.set.target.name"))
            .setCallback(() -> new InputSignGui(player, null) {
                @Override
                public void onClose() {
                    String newValue = this.joinStrings();
                    if (newValue.isBlank()) {
                        TextHelper.sendActionBarByKey(player, "works.work.add.empty_name");
                        return;
                    }
                    work.name = newValue;
                    TextHelper.sendMessageByKey(player, "works.work.set.done", work.name);
                }
            }.open())
        );
        gui.addSlot(new GuiElementBuilder()
            .setItem(Items.CHERRY_HANGING_SIGN)
            .setName(TextHelper.getTextByKey(player, "works.work.set.target.introduction"))
            .setCallback(() -> new InputSignGui(player, null) {
                @Override
                public void onClose() {
                    String newIntroduction = this.joinStrings();
                    if (!newIntroduction.isBlank()) {
                        work.introduction = newIntroduction;
                        TextHelper.sendMessageByKey(player, "works.work.set.done", work.introduction);
                    }
                }
            }.open())
        );
        gui.addSlot(new GuiElementBuilder()
            .setItem(Items.END_PORTAL_FRAME)
            .setName(TextHelper.getTextByKey(player, "works.work.set.target.position"))
            .setCallback(() -> {
                work.level = EntityHelper.getServerWorld(player).getRegistryKey().getValue().toString();
                work.x = player.getPos().x;
                work.y = player.getPos().y;
                work.z = player.getPos().z;
                TextHelper.sendMessageByKey(player, "works.work.set.done", "(%s, %f, %f, %f)".formatted(work.level, work.x, work.y, work.z));
                gui.close();
            })
        );
        gui.addSlot(new GuiElementBuilder()
            .setItem(Items.PAINTING)
            .setName(TextHelper.getTextByKey(player, "works.work.set.target.icon"))
            .setCallback(() -> {
                ItemStack mainHandItem = player.getMainHandStack();
                if (mainHandItem.isEmpty()) {
                    TextHelper.sendActionBarByKey(player, "works.work.set.target.icon.no_item");
                    gui.close();
                    return;
                }
                work.icon = Registries.ITEM.getId(mainHandItem.getItem()).toString();
                TextHelper.sendMessageByKey(player, "works.work.set.done", work.icon);
                gui.close();
            })
        );

        gui.addSlot(new GuiElementBuilder()
            .setItem(Items.BARRIER)
            .setName(TextHelper.getTextByKey(player, "works.work.set.target.delete"))
            .setCallback(() -> new ConfirmSignGui(player) {
                @Override
                public void onConfirm() {
                    WorksInitializer.works.model().works.remove(work);
                    TextHelper.sendActionBarByKey(player, "works.work.delete.done");
                }
            }.open())

        );

        gui.setSlot(8, GuiHelper.makePreviousPageButton(player)
            .setCallback(parentGui::open)
        );

        // let's open it now
        gui.open();
    }

    public List<Text> ofLore(ServerPlayerEntity player) {
        List<Text> ret = new ArrayList<>();
        ret.add(TextHelper.getTextByKey(player, "works.work.prop.creator", this.creator));
        if (this.introduction != null) {
            ret.add(TextHelper.getTextByKey(player, "works.work.prop.introduction", this.introduction));
        }
        ret.add(TextHelper.getTextByKey(player, "works.work.prop.time", ChronosUtil.toDefaultDateFormat(this.createTimeMS)));
        ret.add(TextHelper.getTextByKey(player, "works.work.prop.dimension", this.level));
        ret.add(TextHelper.getTextByKey(player, "works.work.prop.coordinate", this.x, this.y, this.z));
        return ret;
    }

}



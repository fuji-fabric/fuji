package io.github.sakurawald.fuji.module.initializer.works.structure.work.abst;

import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.sakurawald.fuji.core.auxiliary.ChronosUtil;
import io.github.sakurawald.fuji.core.auxiliary.RandomUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.RegistryHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.module.initializer.works.gui.WorkGeneralSettingsGui;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.item.Item;
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
        makeGeneralSettingsGui(player, parentGui, work);
    }

    private static void makeGeneralSettingsGui(@NotNull ServerPlayerEntity player, @NotNull SimpleGui parentGui, Work work) {
        new WorkGeneralSettingsGui(parentGui, player, work)
            .open();
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



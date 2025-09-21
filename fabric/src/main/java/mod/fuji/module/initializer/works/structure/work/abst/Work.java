package mod.fuji.module.initializer.works.structure.work.abst;

import eu.pb4.sgui.api.gui.SimpleGui;
import mod.fuji.core.auxiliary.ChronosUtil;
import mod.fuji.core.auxiliary.RandomUtil;
import mod.fuji.core.auxiliary.minecraft.ItemStackHelper;
import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.auxiliary.minecraft.RegistryHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.config.interfaces.ObjectTypeStringGetter;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.module.initializer.works.gui.WorkGeneralSettingsGui;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Data
public abstract class Work implements ObjectTypeStringGetter {

    @Document(id = 1751825471703L, value = "The type of this work.")
    public String type;

    @Document(id = 1751825477130L, value = "The unique id of this work.")
    public String id;

    @Document(id = 1751825483661L, value = "The create time of this work.")
    public long createTimeMS;

    @Document(id = 1751825488761L, value = "Which player created this work.")
    public String creator;

    @Document(id = 1751825493597L, value = "The display name of this work.")
    public String name;

    @Document(id = 1751825497322L, value = "The introduction of this work.")
    public @Nullable String introduction;

    public String level;
    public double x;
    public double y;
    public double z;
    public float yaw;
    public float pitch;

    @Document(id = 1751825502471L, value = "The display item of this work.")
    public @Nullable String icon;

    public Work(@NotNull ServerPlayerEntity player, String name) {
        this.type = getObjectTypeString();
        this.id = RandomUtil.randomUUID();
        this.createTimeMS = System.currentTimeMillis();
        this.creator = PlayerHelper.getPlayerName(player);
        this.name = name;
        this.introduction = null;
        this.level = RegistryHelper.getIdAsString(PlayerHelper.getServerWorld(player));
        this.x = player.getPos().x;
        this.y = player.getPos().y;
        this.z = player.getPos().z;
        this.yaw = player.getYaw();
        this.pitch = player.getPitch();
        this.icon = null;
    }

    protected abstract Item getDefaultEntityIcon();

    public ItemStack getEntityIcon() {
        if (this.icon == null) {
            return this.getDefaultEntityIcon().getDefaultStack();
        }

        return ItemStackHelper.Parser.parseItemStack(this.icon);
    }

    public abstract void openSpecializedSettingsGui(ServerPlayerEntity player, SimpleGui parentGui);

    public void openGeneralSettingsGui(@NotNull ServerPlayerEntity player, @NotNull SimpleGui parentGui) {
        Work work = this;
        new WorkGeneralSettingsGui(parentGui, player, work)
            .open();
    }

    public List<Text> ofLore(ServerPlayerEntity player) {
        List<Text> ret = new ArrayList<>();
        ret.add(TextHelper.getTextByKey(player, "works.work.prop.creator", this.creator));
        if (this.introduction != null) {
            ret.add(TextHelper.getTextByKey(player, "works.work.prop.introduction", this.introduction));
        }
        ret.add(TextHelper.getTextByKey(player, "works.work.prop.time", ChronosUtil.Formatter.formatDate(this.createTimeMS)));
        ret.add(TextHelper.getTextByKey(player, "works.work.prop.dimension", this.level));
        ret.add(TextHelper.getTextByKey(player, "works.work.prop.coordinate", this.x, this.y, this.z));
        return ret;
    }

}



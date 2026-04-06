package mod.fuji.module.initializer.head.structure;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import lombok.Data;
import lombok.NoArgsConstructor;
import mod.fuji.core.auxiliary.minecraft.GuiHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@Data
@NoArgsConstructor
public class Head {

    public @NotNull String name;

    public UUID uuid;

    public String value;

    @Nullable
    public String tags;

    public Head(@NotNull String name, UUID uuid, String value, @Nullable String tags) {
        this.name = name;
        this.uuid = uuid;
        this.value = value;
        this.tags = tags;
    }

    public Head(String name, UUID uuid, String value) {
        this(name, uuid, value, null);
    }

    public Head(UUID uuid, String value) {
        this("", uuid, value, null);
    }

    public @NotNull String getTagsOrEmpty() {
        return tags == null ? "" : tags;
    }

    public ItemStack toItemStack() {
        GuiElementBuilder builder = new GuiElementBuilder()
            .setItem(Items.PLAYER_HEAD)
            .setName(Component.literal(name));

        GuiHelper.PlayerSkull.setSkullOwner(builder, value, null, uuid);

        return builder.asStack();
    }
}

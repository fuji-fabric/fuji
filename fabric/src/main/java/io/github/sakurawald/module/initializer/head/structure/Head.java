package io.github.sakurawald.module.initializer.head.structure;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class Head {

    public final @NotNull String name;
    public final UUID uuid;
    public final String value;
    @Nullable
    public final String tags;

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
        return new GuiElementBuilder()
            .setItem(Items.PLAYER_HEAD)
            .setName(Text.literal(name))
            .setSkullOwner(value, null, uuid)
            .asStack();
    }
}

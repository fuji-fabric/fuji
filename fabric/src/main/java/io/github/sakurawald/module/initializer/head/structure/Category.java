package io.github.sakurawald.module.initializer.head.structure;

import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public enum Category {
    ALPHABET("alphabet",
        new Head(
            UUID.fromString("1f961930-4e97-47b7-a5a1-2cc5150f3764"),
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmMzNWU3MjAyMmUyMjQ5YzlhMTNlNWVkOGE0NTgzNzE3YTYyNjAyNjc3M2Y1NDE2NDQwZDU3M2E5MzhjOTMifX19").toItemStack()
    ),
    ANIMALS("animals",
        new Head(
            UUID.fromString("6554e785-2a74-481a-9aac-06fc18620a57"),
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWQxN2U0OGM5MjUzZTY3NDczM2NlYjdiYzNkYTdmNTIxNTFlNTI4OWQwMjEyYzhmMmRkNzFlNDE2ZTRlZTY1In19fQ=="
        ).toItemStack()
    ),
    BLOCKS("blocks",
        new Head(
            UUID.fromString("795e1ad8-de6d-4edc-a1b5-4e6aad038403"),
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODQ0OWI5MzE4ZTMzMTU4ZTY0YTQ2YWIwZGUxMjFjM2Q0MDAwMGUzMzMyYzE1NzQ5MzJiM2M4NDlkOGZhMGRjMiJ9fX0="
        ).toItemStack()
    ),
    DECORATION("decoration",
        new Head(
            UUID.fromString("f3244903-0c01-4f8d-bbc2-4b13338c6a10"),
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmQyYWNiZTVkMmM2MmU0NTViMGQ4ZTY5YzdmNmIwMWJiNjg5NzVmYmZjZmQ5NWMyNzViM2Y5MTYzMTU4NTE5YyJ9fX0="
        ).toItemStack()
    ),
    FOOD_DRINKS("food-drinks",
        new Head(
            UUID.fromString("187ab05d-1d27-450b-bea8-a723fd1d3b4a"),
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWZiNDhlMmI5NjljNGMxYjg2YzVmODJhMmUyMzc5OWY0YTZmMzFjZTAwOWE1ZjkyYjM5ZjViMjUwNTdiMmRkMCJ9fX0="
        ).toItemStack()
    ),
    HUMANS("humans",
        new Head(
            UUID.fromString("68cd5f2e-01d3-4ac8-882e-2f7ce487b33b"),
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGNiN2ExNWVkYTFjYmU0N2E4ZDVkN2Y3ODBlODliYmMzNWUwYzE3N2ZjYjljNjQ4MGExMWIwMmNjODE2NWMxYyJ9fX0="
        ).toItemStack()
    ),
    HUMANOID("humanoid",
        new Head(
            UUID.fromString("0d8391c2-1748-4869-8631-935ff2d55e07"),
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGNhOGVmMjQ1OGEyYjEwMjYwYjg3NTY1NThmNzY3OWJjYjdlZjY5MWQ0MWY1MzRlZmVhMmJhNzUxMDczMTVjYyJ9fX0="
        ).toItemStack()
    ),
    MISC("miscellaneous",
        new Head(
            UUID.fromString("13affe21-698a-4a5e-aff1-ad5183d5f810"),
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTFlNDJiOGY1MGZlZDgyOGQ0Yjk4MWMyN2NhMTNkMDcxY2U4NjNmNjE1NDBiMjc2MzgyNjZmNzcyZDQxZCJ9fX0="
        ).toItemStack()
    ),
    MONSTERS("monsters",
        new Head(
            UUID.fromString("a1d05a1e-5937-48ad-973f-70b922d025be"),
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjI2MDJkNWIzNjJhYTE2MzZkMzVhZjIwZmM3MGQyZTc5NDEzMmVhNjRkNjJkMjNmNTVkYjg1MTVhMGM2MTljNyJ9fX0="
        ).toItemStack()
    ),
    PLANTS("plants", new Head(
        UUID.fromString("6b063c51-34b4-4fcb-be0d-a6aff0783328"),
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTAxOWVjZTEyNWVjNzlmOTUxNzhlMWFmNGRhMmE4Yjk4MmRlNzFlZDQyYzMxY2FjNGIxZDJmNjY1MzU1ZGY1YSJ9fX0="
    ).toItemStack());

    public final String name;
    public final ItemStack icon;

    Category(String name, ItemStack icon) {
        this.name = name;
        this.icon = icon;
    }

    public @NotNull ItemStack toItemStack(ServerPlayerEntity player) {
        icon.set(DataComponentTypes.CUSTOM_NAME, this.getText(player));
        return icon;
    }

    public @NotNull Text getText(ServerPlayerEntity player) {
        return TextHelper.getTextByKey(player, "head.category." + name);
    }

}

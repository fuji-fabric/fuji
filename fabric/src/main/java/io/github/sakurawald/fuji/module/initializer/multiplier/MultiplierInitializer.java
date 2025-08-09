package io.github.sakurawald.fuji.module.initializer.multiplier;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.LuckpermsHelper;
import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.core.document.annotation.DocStringProvider;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.document.annotation.TestCase;
import io.github.sakurawald.fuji.core.document.descriptor.MetaDescriptor;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@Document(id = 1751978330624L, value = """
    This module allows you to `multiply` some `numeric values` in vanilla Minecraft.

    Now supported `numeric types`:
    1. `damage`: The damage to a player.
    2. `experience`: The gained experience of a player.
    """)
@ColorBox(id = 1751978406823L, color = ColorBox.ColorBlockTypes.EXAMPLE, value = """
    ◉ Double the damage from `zombie` to a player.
    Issue: `/lp group default meta set fuji.multiplier.damage.minecraft:zombie 2`

    ◉ Cancel the `fall damage` to a player.
    Issue: `/lp group default meta set fuji.multiplier.damage.minecraft:fall 0`

    ◉ Double `all` damages to a player.
    Issue: `/lp group default meta set fuji.multiplier.damage.all 2`

    ◉ Half `all` damages to a player.
    Issue: `/lp group default meta set fuji.multiplier.damage.all 0.5`

    ◉ Double all experience a player gained.
    Issue: `/lp group default meta set fuji.multiplier.experience.all 2`
    """)
public class MultiplierInitializer extends ModuleInitializer{

    @DocStringProvider(id = 1752000356004L, value = """
        Specify the `multiply factor` for a specified `type` for this player.
        """)
    public static final MetaDescriptor<Float> MULTIPLIER_META = new MetaDescriptor<>("fuji.multiplier.<multiplier-type>.<id>", Float::valueOf, 1752000356004L);

    @TestCase(action = "Summon a fake player using `/player 1 spawn` and throw exp bottle to it.", targets = "Test the compatibility between `luckperms` and `carpet`'s fake player.")
    public static float transform(@NotNull ServerPlayerEntity player, String type, String key, float f) {
        Optional<Float> meta = LuckpermsHelper.getMeta(player.getUuid(), MULTIPLIER_META, type, key);
        return meta.map(factor -> f * factor).orElse(f);
    }
}

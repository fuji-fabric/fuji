package io.github.sakurawald.fuji.module.initializer.world.gamerule;

import io.github.sakurawald.fuji.core.config.mapper.GsonMapper;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.world.gamerule.config.model.WorldGameRuleConfigModel;
import io.github.sakurawald.fuji.module.initializer.world.gamerule.config.adapter.BooleanGameRuleMapAdapter;
import io.github.sakurawald.fuji.module.initializer.world.gamerule.structure.GameRuleDescriptor;
import io.github.sakurawald.fuji.module.initializer.world.gamerule.config.adapter.IntegerGameRuleMapAdapter;
import it.unimi.dsi.fastutil.objects.Reference2BooleanMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import java.util.Optional;
import net.minecraft.world.GameRules;

@Document(id = 1752577892546L, value = """
    This module allows you to customize the `per-dimension gamerule`.
    """)
@ColorBox(id = 1753064698840L, color = ColorBox.ColorBoxTypes.NOTE, value = """
    ◉ How it works?
    In internal Minecraft, each `dimension` has a function named `getGameRules()`.
    It returns the `gamerules` of this `dimension`.

    The vanilla Minecraft only returns the `gamerules` of `minecraft:overworld`.
    Fuji modify the `getGameRules()` function, to let it return the `per-dimension gamerules`.

    ◉ How can I configure the `per-dimension gamerules`?
    You can modify the config file directly, and issue `/fuji reload` to apply changes.

    ◉ Can I use this module in vanilla dimensions?
    Yes, you can.
    """)
@ColorBox(id = 1752292508145L, color = ColorBox.ColorBoxTypes.TIP, value = """
    ◉ The logic of `/gamerule` command.
    The `/gamerule` command `only` operates on `minecraft:overworld` dimension.

    To see the `true info` of `a specified dimension`, you should use `/world info` command.

    ◉ Set the `per-dimension gamerules` using commands.
    You can install the `WorldGameRules` mod to provide such commands.
    See https://github.com/DrexHD/WorldGameRules
    """)
public class WorldGameRuleInitializer extends ModuleInitializer {

    private static final BaseConfigurationHandler<WorldGameRuleConfigModel> config = ObjectConfigurationHandler.ofModule(BaseConfigurationHandler.CONFIG_JSON_LITERAL, WorldGameRuleConfigModel.class);

    public static Optional<GameRuleDescriptor> getEffectiveGameRuleDescriptor(String dimensionId) {
        return config.model().gameRules
            .stream()
            .filter(it -> it.enable
                && it.dimensionId.equals(dimensionId))
            .findFirst();
    }

    public static GameRules getEffectiveGameRules(String dimensionId, GameRules original) {
        Optional<GameRuleDescriptor> effectiveGameRuleDescriptor = WorldGameRuleInitializer
            .getEffectiveGameRuleDescriptor(dimensionId);
        return effectiveGameRuleDescriptor
            .map(GameRuleDescriptor::asVanillaGameRules)
            .orElse(original);
    }

    @Override
    protected void registerGsonTypeAdapters() {
        GsonMapper.registerGsonTypeAdapter(Reference2BooleanMap.class, new BooleanGameRuleMapAdapter());
        GsonMapper.registerGsonTypeAdapter(Reference2IntMap.class, new IntegerGameRuleMapAdapter());
    }
}

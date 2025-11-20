package mod.fuji.module.initializer.cleaner;

import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandRequirement;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.core.config.handler.abst.BaseConfigurationHandler;
import mod.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import mod.fuji.core.document.annotation.ColorBox;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.module.initializer.ModuleInitializer;
import mod.fuji.module.initializer.cleaner.config.model.CleanerConfigModel;
import mod.fuji.module.initializer.cleaner.config.transformer.CleanerV1SchemaTransformer;
import mod.fuji.module.initializer.cleaner.service.CleanerService;
import net.minecraft.commands.CommandSourceStack;

@Document(id = 1751826898176L, value = """
    This module provides an `entity cleaner`.
    It monitors specified conditions and `clean` entities that `match defined rules`.
    """)
@ColorBox(id = 1751870582940L, color = ColorBox.ColorBoxTypes.NOTE, value = """
    ◉ The vanilla Minecraft `Item Entity` de-spawning.
    In `vanilla Minecraft`, all `item entities` automatically de-spawn after `6000 ticks (5 minutes)`.
    In most case, the vanilla `item de-spawning mechanism` is sufficient to meet your needs.

    It's strongly recommended to use the `cleaner` module only for handling `special case`.
    For example, if your players have built a `large mob farm`, which produces excessive amount of `ender pearls`.
    You may define a `clean matcher` to match the `ender pearl` entity.

    ◉ Cleaning scope of the `cleaner` module.
    This module exclusively targets `entities`.
    It can not be used to clean `blocks` or `block entities`
    <blue>NOTE: The `block entity` is not a true `entity` within Minecraft internal.
    """)
@ColorBox(id = 1756789894197L, color = ColorBox.ColorBoxTypes.NOTE, value = """
    ◉ Supported `cleanup methods`
    - `KILL`: Remove the entity as if it had `died`, triggering the `entity death event`. (Loot will be dropped.)
    - `DISCARD`: Remove the entity by `discarding` it, without triggering any events. (No loot will be dropped.)

    NOTE: For `item entity`, there is no significant difference between the available cleanup methods.
    """)
@ColorBox(id = 1756790951617L, color = ColorBox.ColorBoxTypes.NOTE, value = """
    ◉ What is a `translatable key`?
    A `translatable key` is an identifier used internally by Minecraft to reference `items`, `block-based items`, and other `entities`.
    It corresponds to the keys defined in Minecraft's `translation files`.
    An example translation file can be found here:
    https://github.com/fuji-fabric/fuji/blob/dev/.github/files/en_us.json

    Common key formats include:
    - `item.minecraft.*`
    - `block.minecraft.*`
    - `entity.minecraft.*`
    """)
@ColorBox(id = 1756789316771L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ Define a `matcher` to clean `item entities`.
    A `matcher` identifies entities using their `translatable key`.
    - For `minecraft:gold_ingot` item-entity, the key is `item.minecraft.gold_ingot`
    - For `minecraft:gold_block` item-entity, the key is `block.minecraft.gold_block`

    Although the key for a block-based item begins with `block.`, it still refers to an `item entity` whose stack contains `minecraft:gold_block`.

    ◉ Define a `matcher` to clean `living entity`.
    To enable cleaning of living entities:
    1. Set `ignore_living_entity` to `false`.
    2. Define a `matcher` for the target entity, for example: `entity.minecraft.skeleton`.
    """)



@CommandNode("cleaner")
@CommandRequirement(level = 4)
public class CleanerInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<CleanerConfigModel> config = ObjectConfigurationHandler
        .ofModule(BaseConfigurationHandler.CONFIG_JSON_LITERAL, CleanerConfigModel.class)
        .installTransformer(new CleanerV1SchemaTransformer());

    @Document(id = 1756788946930L, value = "Perform entity cleanup, and generate a cleanup report if any entities are removed.")
    @CommandNode("clean")
    private static int $clean(@CommandSource CommandSourceStack source) {
        CleanerService.cleanEntities();
        return CommandHelper.Return.SUCCESS;
    }

}

package io.github.sakurawald.fuji.module.initializer.cleaner;

import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.EntityHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.ItemStackHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.WorldHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.event.impl.ServerLifecycleEvents;
import io.github.sakurawald.fuji.core.manager.Managers;
import io.github.sakurawald.fuji.core.service.type_formatter.TypeFormatter;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.cleaner.config.model.CleanerConfigModel;
import io.github.sakurawald.fuji.module.initializer.cleaner.job.CleanerJob;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

@Document(id = 1751826898176L, value = """
    This module provides the `entity` cleaner.
    To remove specified entities automatically.
    """)
@ColorBox(id = 1751870582940L, color = ColorBox.ColorBoxTypes.NOTE, value = """
    You should only use this module to clean some edge-case entity.
    The vanilla Minecraft also has a `cleaner` to remove dropped items.
    In normal case, you can rely on the `vanilla cleaner`.
    But for some special case, you may want to use this module.
    To clean some `annoying dropped items` or even `entities` (`pig` or `boat`).

    Yeah, the `vanilla cleaner` only cleans `dropped items`.
    But this module, allows you to define rules, to clean `dropped items` and `entities`.
    """)
@ColorBox(id = 1751870585373L, color = ColorBox.ColorBoxTypes.NOTE, value = """
    For safety, the `cleaner` will `always ignore` the following types:
    1. player
    2. any block attached entity (e.g. leash_knot)
    3. any vehicle entity (e.g. minecart, boat)
    """)


@CommandNode("cleaner")
@CommandRequirement(level = 4)
public class CleanerInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<CleanerConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, CleanerConfigModel.class);

    @SuppressWarnings("RedundantIfStatement")
    private static boolean shouldIgnoreEntity(Entity entity) {
        /* Always ignore these entities. */
        if (entity.getType().equals(EntityType.PLAYER)) return true;
        if (EntityHelper.isBlockAttachedEntity(entity)) return true;
        if (EntityHelper.isVehicleEntity(entity)) return true;

        /* Ignore entities based on config. */
        var config = CleanerInitializer.config.model().ignore;
        if (config.ignore_item_entity && entity instanceof ItemEntity) return true;
        if (config.ignore_living_entity && entity.isLiving()) return true;
        if (config.ignore_named_entity) {
            if (entity.hasCustomName()) return true;
            if (entity instanceof ItemEntity ie) {
                ItemStack stack = ie.getStack();
                if (ItemStackHelper.CustomName.hasCustomName(stack)) {
                    return true;
                }
            }
        }
        if (config.ignore_entity_with_vehicle && entity.hasVehicle()) return true;
        if (config.ignore_entity_with_passengers && entity.hasPassengers()) return true;
        if (config.ignore_glowing_entity && entity.isGlowing()) return true;
        if (config.ignore_leashed_entity && EntityHelper.isLeashed(entity)) return true;

        /* Should not ignore this entity. */
        return false;
    }

    private static boolean shouldRemoveThisEntity(String key, int age) {
        Map<String, Integer> key2age = config.model().key2age;
        return key2age.containsKey(key)
            && age >= key2age.get(key);
    }

    @Document(id = 1751826901492L, value = "Trigger the cleaner once.")
    @CommandNode("clean")
    public static int $clean() {
        /* Clean entities in the server. */
        Map<String, Integer> cleanedEntities = new HashMap<>();
        for (ServerWorld world : WorldHelper.getWorlds()) {
            for (Entity entity : world.iterateEntities()) {
                if (shouldIgnoreEntity(entity)) continue;

                String key = getTranslatableKey(entity);

                if (shouldRemoveThisEntity(key, entity.age)) {
                    Integer originalAmount = cleanedEntities.getOrDefault(key, 0);
                    cleanedEntities.put(key, originalAmount + 1);
                    entity.discard();
                }
            }
        }

        /* Send cleaning report. */
        sendCleanerBroadcast(cleanedEntities);
        return CommandHelper.Return.SUCCESS;
    }

    private static String getTranslatableKey(Entity entity) {
        String key;
        if (entity instanceof ItemEntity itemEntity) {
            key = itemEntity.getStack().getItem().getTranslationKey();
        } else {
            key = entity.getType().getTranslationKey();
        }
        return key;
    }

    private static void sendCleanerBroadcast(Map<String, Integer> counter) {
        if (counter.isEmpty()) return;

        LogUtil.info("Remove entities: {}", counter);

        Text hoverText =
            Text.empty()
                .formatted(Formatting.GOLD)
                .append(TypeFormatter.formatTypes(null, counter));

        for (ServerPlayerEntity player : PlayerHelper.Lookup.getOnlinePlayers()) {
            int numberOfCleanedEntities = counter.values().stream().mapToInt(Integer::intValue).sum();
            MutableText reportText = Text.empty()
                .append(TextHelper.getTextByKey(player, "cleaner.broadcast", numberOfCleanedEntities))
                .fillStyle(
                    Style.EMPTY
                        .withHoverEvent(TextHelper.Events.HoverEvent.makeShowTextAction(hoverText)));
            TextHelper.sendText(player, reportText);
        }
    }

    @Override
    protected void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            CleanerJob cleanerJob = new CleanerJob();
            Managers.getScheduleManager().scheduleJob(cleanerJob);
        });
    }

}

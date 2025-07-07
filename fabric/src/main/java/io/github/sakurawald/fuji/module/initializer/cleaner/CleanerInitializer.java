package io.github.sakurawald.fuji.module.initializer.cleaner;

import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.EntityHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.ItemStackHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.event.impl.ServerLifecycleEvents;
import io.github.sakurawald.fuji.core.manager.Managers;
import io.github.sakurawald.fuji.core.service.type_formatter.TypeFormatter;
import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.cleaner.config.model.CleanerConfigModel;
import io.github.sakurawald.fuji.module.initializer.cleaner.job.CleanerJob;
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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Document(id = 1751826898176L, value = """
    This module provides the `entity` cleaner.
    To remove specified entities automatically.
    """)

@ColorBox(color = ColorBox.ColorBlockTypes.NOTE, value = """
    You should only use this module to clean some edge-case entity.
    The vanilla Minecraft also has a `cleaner` to remove dropped items.
    In normal case, you can rely on the `vanilla cleaner`.
    But for some special case, you may want to use this module.
    To clean some `annoying dropped items` or even `entities` (`pig` or `boat`).

    Yeah, the `vanilla cleaner` only cleans `dropped items`.
    But this module, allows you to define rules, to clean `dropped items` and `entities`.
    """)

@ColorBox(color = ColorBox.ColorBlockTypes.NOTE, value = """
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
    private static boolean ignoreEntity(Entity entity) {
        if (entity.getType().equals(EntityType.PLAYER)) return true;
        if (EntityHelper.isBlockAttachedEntity(entity)) return true;
        if (EntityHelper.isVehicleEntity(entity)) return true;

        var config = CleanerInitializer.config.model().ignore;

        if (config.ignore_item_entity && entity instanceof ItemEntity) return true;
        if (config.ignore_living_entity && entity.isLiving()) return true;
        if (config.ignore_named_entity) {
            if (entity.hasCustomName()) return true;
            if (entity instanceof ItemEntity ie) {
                ItemStack stack = ie.getStack();
                if (ItemStackHelper.hasCustomName(stack)) {
                    return true;
                }
            }
        }
        if (config.ignore_entity_with_vehicle && entity.hasVehicle()) return true;
        if (config.ignore_entity_with_passengers && entity.hasPassengers()) return true;
        if (config.ignore_glowing_entity && entity.isGlowing()) return true;
        if (config.ignore_leashed_entity && EntityHelper.isLeashed(entity)) return true;

        return false;
    }

    private static boolean shouldRemove(String key, int age) {
        Map<String, Integer> regex2age = config.model().key2age;
        return regex2age.containsKey(key) && age >= regex2age.get(key);
    }

    @Document(id = 1751826901492L, value = "Trigger the cleaner once.")
    @CommandNode("clean")
    public static int clean() {
        CompletableFuture.runAsync(() -> {
            Map<String, Integer> counter = new HashMap<>();

            for (ServerWorld world : ServerHelper.getServer().getWorlds()) {
                for (Entity entity : world.iterateEntities()) {
                    if (ignoreEntity(entity)) continue;

                    String key;
                    if (entity instanceof ItemEntity itemEntity) {
                        key = itemEntity.getStack().getItem().getTranslationKey();
                    } else {
                        key = entity.getType().getTranslationKey();
                    }

                    if (shouldRemove(key, entity.age)) {
                        counter.put(key, counter.getOrDefault(key, 0) + 1);
                        entity.discard();
                    }
                }
            }

            // output
            sendCleanerBroadcast(counter);
        });

        return CommandHelper.Return.SUCCESS;
    }

    private static void sendCleanerBroadcast(Map<String, Integer> counter) {
        // avoid spam
        if (counter.isEmpty()) return;

        LogUtil.info("Remove entities: {}", counter);

        Text hoverText =
            Text.empty()
                .formatted(Formatting.GOLD)
                .append(TypeFormatter.formatTypes(null, counter));

        for (ServerPlayerEntity player : ServerHelper.getOnlinePlayers()) {
            MutableText text = Text.empty()
                .append(TextHelper.getTextByKey(player, "cleaner.broadcast", counter.values().stream().mapToInt(Integer::intValue).sum()))
                .fillStyle(
                    Style.EMPTY
                        .withHoverEvent(TextHelper.Events.HoverEvent.makeShowTextAction(hoverText)));
            player.sendMessage(text);
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

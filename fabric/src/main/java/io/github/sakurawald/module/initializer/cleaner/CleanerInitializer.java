package io.github.sakurawald.module.initializer.cleaner;

import io.github.sakurawald.core.annotation.Document;
import io.github.sakurawald.core.auxiliary.LogUtil;
import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.core.auxiliary.minecraft.EntityHelper;
import io.github.sakurawald.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.core.auxiliary.minecraft.StackHelper;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.command.annotation.CommandNode;
import io.github.sakurawald.core.command.annotation.CommandRequirement;
import io.github.sakurawald.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.core.event.impl.ServerLifecycleEvents;
import io.github.sakurawald.core.structure.TypeFormatter;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import io.github.sakurawald.module.initializer.cleaner.config.model.CleanerConfigModel;
import io.github.sakurawald.module.initializer.cleaner.job.CleanerJob;
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

@Document("""
    This module provides the `entity` cleaner.
    To remove specified entities automatically.
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
                if (StackHelper.hasCustomName(stack)) {
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

    @CommandNode("clean")
    @Document("Trigger the cleaner manually.")
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

        for (ServerPlayerEntity player : ServerHelper.getPlayers()) {
            MutableText text = Text.empty()
                .append(TextHelper.getTextByKey(player, "cleaner.broadcast", counter.values().stream().mapToInt(Integer::intValue).sum()))
                .fillStyle(
                    Style.EMPTY
                        .withHoverEvent(TextHelper.HoverEvent.makeShowTextAction(hoverText)));
            player.sendMessage(text);
        }
    }

    @Override
    protected void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> new CleanerJob().schedule());
    }

}

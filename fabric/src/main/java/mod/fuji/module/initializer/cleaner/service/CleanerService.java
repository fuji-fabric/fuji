package mod.fuji.module.initializer.cleaner.service;

import mod.fuji.core.auxiliary.LogUtil;
import mod.fuji.core.auxiliary.minecraft.EntityHelper;
import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.auxiliary.minecraft.WorldHelper;
import mod.fuji.core.service.type_formatter.TypeFormatter;
import mod.fuji.module.initializer.cleaner.CleanerInitializer;
import mod.fuji.module.initializer.cleaner.structure.CleanerMatcher;
import mod.fuji.module.initializer.cleaner.structure.CleanupMethod;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CleanerService {

    @SuppressWarnings("RedundantIfStatement")
    private static boolean shouldIgnoreEntity(@Nullable Entity entity) {
        /* Ignore null entity. */
        if (entity == null) return true;

        /* Ignore player entity. */
        if (entity.getType().equals(EntityType.PLAYER)) return true;

        /* Ignore entities by config. */
        var config = CleanerInitializer.config.model().getIgnore();
        if (config.isIgnoreItemEntity() && EntityHelper.Predicates.isItemEntity(entity)) return true;
        if (config.isIgnoreLivingEntity() && EntityHelper.Predicates.isLivingEntity(entity)) return true;
        if (config.isIgnoreNamedEntity() && EntityHelper.Predicates.hasCustomName(entity)) return true;
        if (config.isIgnoreEntityWithVehicle() && EntityHelper.Predicates.hasVehicle(entity)) return true;
        if (config.isIgnoreEntityWithPassengers() && EntityHelper.Predicates.hasPassengers(entity)) return true;
        if (config.isIgnoreLeashedEntity() && EntityHelper.Predicates.isLeashed(entity)) return true;
        if (config.isIgnoreGlowingEntity() && EntityHelper.Predicates.isGlowing(entity)) return true;

        /* Should not ignore this entity. */
        return false;
    }

    private static Optional<CleanerMatcher> findApplicableMatcher(@NotNull String entityKey, int age) {
        return CleanerInitializer.config.model().getMatchers()
            .stream()
            .filter(CleanerMatcher::isEnable)
            .filter(it -> it.isMatch(entityKey, age))
            .findFirst();
    }

    @SuppressWarnings("UnnecessaryReturnStatement")
    private static void cleanEntity(@NotNull Entity entity, @NotNull CleanerMatcher matcher) {
        if (matcher.getCleanupMethod().equals(CleanupMethod.KILL)) {
            EntityHelper.killEntity(entity);
            return;
        }

        if (matcher.getCleanupMethod().equals(CleanupMethod.DISCARD)) {
            EntityHelper.deleteEntity(entity);
            return;
        }
    }

    public static void cleanEntities() {
        /* Clean entities in the server. */
        Map<String, Integer> cleanedEntities = new HashMap<>();

        for (ServerLevel world : WorldHelper.getWorlds()) {
            for (Entity entity : WorldHelper.getEntities(world)) {
                if (shouldIgnoreEntity(entity)) continue;

                String entityKey = EntityHelper.toTranslatableKey(entity);
                int entityCount = EntityHelper.getEntityEffectiveCount(entity);

                findApplicableMatcher(entityKey, entity.tickCount)
                    .ifPresent(matcher -> {
                        cleanedEntities.merge(entityKey, entityCount, Integer::sum);
                        cleanEntity(entity, matcher);
                    });
            }
        }

        /* Send cleaning report. */
        sendCleanerBroadcast(cleanedEntities);
    }

    private static void sendCleanerBroadcast(@NotNull Map<String, Integer> cleanedEntitiesMap) {
        if (cleanedEntitiesMap.isEmpty()) return;
        int cleanedEntitiesCount = cleanedEntitiesMap.values().stream().mapToInt(Integer::intValue).sum();

        LogUtil.info("Cleaned entities: {}", cleanedEntitiesMap);

        Component hoverText =
            Component.empty()
                .withStyle(ChatFormatting.GOLD)
                .append(TypeFormatter.formatTypes(null, cleanedEntitiesMap));

        PlayerHelper.Lookup
            .getOnlinePlayers()
            .forEach(player -> {
                MutableComponent reportText = Component.empty()
                    .append(TextHelper.getTextByKey(player, "cleaner.broadcast", cleanedEntitiesCount))
                    .withStyle(
                        Style.EMPTY
                            .withHoverEvent(TextHelper.Events.HoverEvent.makeShowTextAction(hoverText)));
                TextHelper.sendMessageByText(player, reportText);
            });
    }
}

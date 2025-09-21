package mod.fuji.module.initializer.afk.effect;

import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.config.handler.abst.BaseConfigurationHandler;
import mod.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import mod.fuji.core.event.annotation.EventConsumer;
import mod.fuji.core.event.message.entity.LivingEntityDamageEvent;
import mod.fuji.module.initializer.ModuleInitializer;
import mod.fuji.module.initializer.afk.effect.config.model.AfkEffectConfigModel;
import mod.fuji.module.initializer.afk.service.AfkService;
import net.minecraft.server.network.ServerPlayerEntity;

@Document(id = 1751826206965L, value = """
    This module provides special effects for afk player:
    1. Invulnerable: the afk player is immune to all damage.
    2. Targetable: the afk player can not be targeted by a hostile entity.
    3. Moveable: the afk player can not be moved.
    """)
public class AfkEffectInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<AfkEffectConfigModel> config = ObjectConfigurationHandler.ofModule(BaseConfigurationHandler.CONFIG_JSON_LITERAL, AfkEffectConfigModel.class);

    @EventConsumer
    private static void processInvulnerableEffect(LivingEntityDamageEvent event) {
        if (event.getLivingEntity() instanceof ServerPlayerEntity player) {
            if (AfkEffectInitializer.config.model().invulnerable
                && AfkService.isAfk(player)) {
                event.setDamage(0);
            }
        }

    }
}

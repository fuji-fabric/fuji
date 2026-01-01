package mod.fuji.module.initializer.afk.effect;

import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.config.handler.abst.BaseConfigurationHandler;
import mod.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import mod.fuji.core.event.annotation.EventConsumer;
import mod.fuji.core.event.message.entity.LivingEntityDamageEvent;
import mod.fuji.module.initializer.ModuleInitializer;
import mod.fuji.module.initializer.afk.effect.config.model.AfkEffectConfigModel;
import mod.fuji.module.initializer.afk.service.AfkService;

@Document(id = 1751826206965L, value = """
    This module applies `effects` for a player in `afk` state.
    Supported effects are:
    1. `Invulnerable`: a player in afk state is immune to all `damage`.
    2. `Targetable`: a player in afk state cannot be selected as a `target` by hostile entities.
    3. `Moveable`: the position of a player in afk state can not be `moved` by `external cause`. (Piston, Gravity...)
    """)
public class AfkEffectInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<AfkEffectConfigModel> config = ObjectConfigurationHandler.ofModule(BaseConfigurationHandler.CONFIG_JSON_LITERAL, AfkEffectConfigModel.class);

    @EventConsumer
    private static void processInvulnerableEffect(LivingEntityDamageEvent event) {
        PlayerHelper.Kind.ifServerPlayerEntity(event.getLivingEntity(), player -> {
            if (AfkEffectInitializer.config.model().invulnerable
                && AfkService.isInAfkState(player)) {
                event.setDamage(0);
            }
        });
    }
}

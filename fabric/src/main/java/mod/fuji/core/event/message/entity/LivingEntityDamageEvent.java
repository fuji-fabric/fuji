package mod.fuji.core.event.message.entity;

import mod.fuji.core.event.message.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import org.jetbrains.annotations.NotNull;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class LivingEntityDamageEvent extends BaseEvent {

    @NotNull LivingEntity livingEntity;
    @NotNull DamageSource damageSource;
    float damage;

}

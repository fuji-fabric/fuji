package io.github.sakurawald.fuji.module.mixin.core.authlib;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = PropertyMap.class, remap = false)
public class PropertyMapMixin {

    @Mutable
    @Shadow
    @Final
    private Multimap<String, Property> properties;

    @Inject(method = "<init>", at = @At("RETURN"))
    void makePropertyMapMutable(CallbackInfo ci) {
        // NOTE: The PropertyMap becomes an immutable collection since MC 1.21.9
        this.properties = LinkedHashMultimap.create(properties);
    }

}

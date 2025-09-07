package io.github.sakurawald.fuji.module.mixin;

import io.github.sakurawald.fuji.core.manager.impl.module.ModuleLoadDeterminer;
import io.github.sakurawald.fuji.core.structure.MixinApplicationInfo;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;


public class GlobalMixinConfigPlugin implements IMixinConfigPlugin {

    public static Map<String, MixinApplicationInfo> mixinApplicationInfoMap = new HashMap<>();

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        boolean applied = ModuleLoadDeterminer.shouldLoadThis(mixinClassName);
        mixinApplicationInfoMap
            .computeIfAbsent(mixinClassName, key -> new MixinApplicationInfo(targetClassName, mixinClassName, applied, null, null));
        return applied;
    }

    @Override
    public void onLoad(String mixinPackage) {
        // no-op
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
        // no-op
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        Optional
            .ofNullable(mixinApplicationInfoMap.get(mixinClassName))
            .ifPresent(it -> {
                it.setPhase(mixinInfo.getPhase());
                it.setPriority(mixinInfo.getPriority());
            });
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        // no-op
    }
}

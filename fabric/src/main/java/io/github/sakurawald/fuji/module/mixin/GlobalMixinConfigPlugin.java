package io.github.sakurawald.fuji.module.mixin;

import io.github.sakurawald.fuji.core.manager.impl.module.ModuleManager;
import io.github.sakurawald.fuji.core.structure.MixinApplicationInfo;
import java.util.ArrayList;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;


public class GlobalMixinConfigPlugin implements IMixinConfigPlugin {

    public static List<MixinApplicationInfo> mixinApplicationInfoList = new ArrayList<>();

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        boolean applied = ModuleManager.shouldWeLoadThis(mixinClassName);
        mixinApplicationInfoList.add(new MixinApplicationInfo(targetClassName, mixinClassName, applied));
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
        // no-op
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        // no-op
    }
}

package io.github.sakurawald.fuji.module.initializer.fuji.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.GuiHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.gui.component.gui.PagedGui;
import io.github.sakurawald.fuji.core.manager.impl.module.ModuleManager;
import io.github.sakurawald.fuji.core.structure.MixinApplicationInfo;
import io.github.sakurawald.fuji.module.mixin.GlobalMixinConfigPlugin;
import java.util.List;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MixinsInspectionGui extends PagedGui<MixinApplicationInfo> {

    public MixinsInspectionGui(@Nullable SimpleGui parent, @NotNull ServerPlayerEntity player, @NotNull List<MixinApplicationInfo> entities, int pageIndex) {
        super(parent, player, TextHelper.getTextByKey(player, "mixin.application"), entities, pageIndex);
    }

    @Override
    protected PagedGui<MixinApplicationInfo> makePage(@Nullable SimpleGui parent, @NotNull ServerPlayerEntity player, Text title, @NotNull List<MixinApplicationInfo> entities, int pageIndex) {
        return new MixinsInspectionGui(parent, player, entities, pageIndex);
    }

    public static MixinsInspectionGui inspectAll(@NotNull ServerPlayerEntity player) {
        List<MixinApplicationInfo> entities = GlobalMixinConfigPlugin.mixinApplicationInfoMap.values().stream().toList();
        return new MixinsInspectionGui(null, player, entities, 0);
    }

    @Override
    protected @NotNull GuiElementInterface toGuiElement(@NotNull MixinApplicationInfo entity) {
        GuiElementBuilder builder = new GuiElementBuilder();

        builder
            .setItem(GuiHelper.Material.fromBooleanValue(entity.isApplied()))
            .setName(TextHelper.getTextByKey(player, "mixin"))
            .setLore(List.of(
                TextHelper.getTextByKey(player, "from_module", ModuleManager.computeJoinedModulePath(entity.getMixinClassName())),
                TextHelper.getTextByKey(player, "mixin.target_class_name", entity.getTargetClassName()),
                TextHelper.getTextByKey(player, "mixin.mixin_class_name", entity.getMixinClassName()),
                TextHelper.getTextByKey(player, "mixin.phase", entity.getPhase()),
                TextHelper.getTextByKey(player, "mixin.applied", entity.isApplied()),
                TextHelper.getTextByKey(player, "mixin.priority", entity.getPriority())
            ));

        return builder.build();
    }
}

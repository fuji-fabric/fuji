package mod.fuji.module.initializer.fuji.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import mod.fuji.core.auxiliary.minecraft.GuiHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.gui.component.gui.PagedGui;
import mod.fuji.core.module.ModulePathResolver;
import mod.fuji.core.structure.MixinApplicationInfo;
import mod.fuji.module.mixin.GlobalMixinConfigPlugin;
import java.util.Comparator;
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
    protected @NotNull PagedGui<MixinApplicationInfo> makePage(@Nullable SimpleGui parent, @NotNull ServerPlayerEntity player, Text title, @NotNull List<MixinApplicationInfo> entities, int pageIndex) {
        return new MixinsInspectionGui(parent, player, entities, pageIndex);
    }

    public static MixinsInspectionGui inspectAll(@NotNull ServerPlayerEntity player) {
        List<MixinApplicationInfo> entities = GlobalMixinConfigPlugin.mixinApplicationInfoMap.values()
            .stream()
            .sorted(Comparator
                .comparing(MixinApplicationInfo::getTargetClassName)
                .thenComparing(MixinApplicationInfo::getMixinClassName)
                .thenComparing(MixinApplicationInfo::isApplied)
                .thenComparing(MixinApplicationInfo::getPriority))
            .toList();
        return new MixinsInspectionGui(null, player, entities, 0);
    }

    @Override
    protected @NotNull GuiElementInterface toGuiElement(@NotNull MixinApplicationInfo entity) {
        GuiElementBuilder builder = new GuiElementBuilder();

        builder
            .setItem(GuiHelper.Material.fromBooleanValue(entity.isApplied()))
            .setName(TextHelper.getTextByKey(player, "mixin"))
            .setLore(List.of(
                TextHelper.getTextByKey(player, "from_module", ModulePathResolver.computeModulePathString(entity.getMixinClassName())),
                TextHelper.getTextByKey(player, "mixin.target_class_name", entity.getTargetClassName()),
                TextHelper.getTextByKey(player, "mixin.mixin_class_name", entity.getMixinClassName()),
                TextHelper.getTextByKey(player, "mixin.phase", entity.getPhase()),
                TextHelper.getTextByKey(player, "mixin.applied", entity.isApplied()),
                TextHelper.getTextByKey(player, "mixin.priority", entity.getPriority())
            ));

        return builder.build();
    }
}

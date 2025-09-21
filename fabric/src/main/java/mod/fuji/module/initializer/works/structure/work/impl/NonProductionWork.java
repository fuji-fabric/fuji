package mod.fuji.module.initializer.works.structure.work.impl;

import eu.pb4.sgui.api.gui.SimpleGui;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.module.initializer.works.structure.WorkType;
import mod.fuji.module.initializer.works.structure.work.abst.Work;
import lombok.NoArgsConstructor;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

@NoArgsConstructor
public class NonProductionWork extends Work {
    public NonProductionWork(@NotNull ServerPlayerEntity player, String name) {
        super(player, name);
    }

    @Override
    public @NotNull String getObjectTypeString() {
        return WorkType.NonProductionWork.name();
    }

    @Override
    protected @NotNull Item getDefaultEntityIcon() {
        return Items.GUNPOWDER;
    }

    @Override
    public void openSpecializedSettingsGui(@NotNull ServerPlayerEntity player, SimpleGui parentGui) {
        TextHelper.sendTextByKey(player, "works.non_production_work.specialized_settings.not_found");
    }
}

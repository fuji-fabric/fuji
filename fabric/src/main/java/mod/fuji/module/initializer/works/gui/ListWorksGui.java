package mod.fuji.module.initializer.works.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.GuiHelper;
import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.auxiliary.minecraft.ServerHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.gui.component.gui.CrudPagedGui;
import mod.fuji.core.gui.component.gui.PagedGui;
import mod.fuji.core.structure.GlobalPos;
import mod.fuji.core.structure.IdentifierIR;
import mod.fuji.module.initializer.works.WorksInitializer;
import mod.fuji.module.initializer.works.structure.work.abst.Work;
import java.util.List;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class ListWorksGui extends CrudPagedGui<Work> {

    public ListWorksGui(ServerPlayer player, @NotNull List<Work> entities, int pageIndex) {
        super(null, player, TextHelper.getTextByKey(player, "works.list.title"), entities, pageIndex);

        /* Place buttons in footer. */
        if (isViewingAllWorks(entities)) {
            GuiHelper.Placer.setSlotInLastLine(this, 5, GuiHelper.Button
                .makeLetterAButton()
                .setName(TextHelper.getTextByKey(player, "works.list.my_works"))
                .setCallback(() -> {
                    String keyword = PlayerHelper.getPlayerName(player);
                    linkCurrentGuiAndSearch(keyword).open();
                })
            );
        } else {
            GuiHelper.Placer.setSlotInLastLine(this, 5, GuiHelper.Button
                .makeHeartButton()
                .setName(TextHelper.getTextByKey(player, "works.list.all_works"))
                .setCallback(() -> new ListWorksGui(player, WorksInitializer.works.model().works, 0).open())
            );
        }
    }

    @Override
    protected @NotNull PagedGui<Work> makePage(@Nullable SimpleGui parent, @NotNull ServerPlayer player, Component title, @NotNull List<Work> entities, int pageIndex) {
        return new ListWorksGui(player, entities, pageIndex);
    }

    private static boolean isViewingAllWorks(@NotNull List<Work> entities) {
        return entities == WorksInitializer.works.model().works;
    }

    @Override
    protected GuiElementBuilder toGuiElementBuilder(Work entity) {
        ServerPlayer player = getPlayer();

        return GuiElementBuilder
            .from(entity.getEntityIcon())
            .setName(TextHelper.getTextByValue(null, entity.name))
            .setLore(entity.ofLore(player));
    }

    @Override
    protected @NotNull String getGuiHelpLoreKey() {
        return "works.list.help.lore";
    }

    @Override
    protected void onCreateEntity() {
        new CreateWorkGui(this.getPlayer())
            .open();
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean canOperateOnThisEntity(@NotNull ServerPlayer player, @NotNull Work work) {
        return PlayerHelper.getPlayerName(player).equals(work.creator)
            || CommandHelper.Requirement.isOperator(player);
    }

    @Override
    protected boolean canCreateEntity() {
        return true;
    }

    @Override
    protected boolean canReadEntity(Work entity) {
        return true;
    }

    @Override
    protected boolean canUpdateEntity(Work entity) {
        return canOperateOnThisEntity(getPlayer(), entity);
    }

    @Override
    protected boolean canDeleteEntity(Work entity) {
        return canOperateOnThisEntity(getPlayer(), entity);
    }

    @Override
    protected void onLeftClickEntity(Work entity) {
        var worldId = IdentifierIR.makeIdentifierOrThrow(entity.level).getNativeValue();
        ResourceKey<Level> worldKey = ResourceKey.create(Registries.DIMENSION, worldId);
        ServerPlayer player = getPlayer();
        ServerLevel dimension = ServerHelper.getServer().getLevel(worldKey);

        if (dimension != null) {
            new GlobalPos(dimension, entity.x, entity.y, entity.z, entity.yaw, entity.pitch)
                .teleport(player);
        } else {
            TextHelper.sendTextByKey(player, "world.dimension.not_found", entity.level);
        }

        this.close();
    }

    @Override
    protected void onRightShiftClickEntity(Work entity) {
        ServerPlayer player = getPlayer();
        if (!canUpdateEntity(entity)) {
            TextHelper.sendTextByKey(player, "works.work.set.no_perm");
            return;
        }
        entity.openSpecializedSettingsGui(player, getBackendGui());
        this.close();
    }

    @Override
    protected void onRightClickEntity(Work entity) {
        ServerPlayer player = getPlayer();
        if (!canUpdateEntity(entity)) {
            TextHelper.sendTextByKey(player, "works.work.set.no_perm");
            return;
        }
        entity.openGeneralSettingsGui(player, getBackendGui());
        this.close();
    }

}

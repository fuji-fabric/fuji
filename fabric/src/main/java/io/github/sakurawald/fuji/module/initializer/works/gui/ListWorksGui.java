package io.github.sakurawald.fuji.module.initializer.works.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.GuiHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.RegistryHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.gui.impl.gui.PagedGui;
import io.github.sakurawald.fuji.core.structure.GlobalPos;
import io.github.sakurawald.fuji.module.initializer.works.WorksInitializer;
import io.github.sakurawald.fuji.module.initializer.works.structure.work.abst.Work;
import java.util.List;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class ListWorksGui extends PagedGui<Work> {

    public ListWorksGui(ServerPlayerEntity player, @NotNull List<Work> entities, int pageIndex) {
        super(null, player, TextHelper.getTextByKey(player, "works.list.title"), entities, pageIndex);

        /* Place buttons in footer. */
        getFooter().setSlot(3, GuiHelper
            .makeAddButton(player)
            .setName(TextHelper.getTextByKey(player, "works.list.add"))
            .setCallback(() -> new CreateWorkGui(player).open())
        );
        getFooter().setSlot(4, GuiHelper
            .makeHelpButton(player)
            .setLore(TextHelper.getTextListByKey(player, "works.list.help.lore")));

        if (isViewingAllWorks(entities)) {
            getFooter().setSlot(5, GuiHelper
                .makeLetterAButton(player)
                .setName(TextHelper.getTextByKey(player, "works.list.my_works"))
                .setCallback(() -> linkCurrentGuiAndSearch(player.getGameProfile().getName()).open())
            );
        } else {
            getFooter().setSlot(5, GuiHelper
                .makeHeartButton(player)
                .setName(TextHelper.getTextByKey(player, "works.list.all_works"))
                .setCallback(() -> new ListWorksGui(player, WorksInitializer.works.model().works, 0).open())
            );
        }
    }

    @Override
    protected PagedGui<Work> make(@Nullable SimpleGui parent, ServerPlayerEntity player, Text title, @NotNull List<Work> entities, int pageIndex) {
        return new ListWorksGui(player, entities, pageIndex);
    }

    private static boolean isViewingAllWorks(@NotNull List<Work> entities) {
        return entities == WorksInitializer.works.model().works;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean canOperateOnThisEntity(@NotNull ServerPlayerEntity player, @NotNull Work work) {
        return PlayerHelper.getPlayerName(player).equals(work.creator)
            || player.hasPermissionLevel(4);
    }

    @Override
    protected GuiElementInterface toGuiElement(@NotNull Work entity) {
        ServerPlayerEntity player = getPlayer();

        return new GuiElementBuilder()
            .setItem(entity.getEntityIcon())
            .setName(TextHelper.getTextByValue(null, entity.name))
            .setLore(entity.ofLore(player))
            .setCallback(handleClick(entity, player)).build();
    }

    private GuiElementInterface.ItemClickCallback handleClick(@NotNull Work entity, ServerPlayerEntity player) {
        return (index, clickType, actionType) -> {
            /* left click -> visit */
            if (clickType.isLeft) {
                RegistryKey<World> worldKey = RegistryKey.of(RegistryKeys.WORLD, RegistryHelper.makeIdentifier(entity.level));
                ServerWorld level = ServerHelper.getServer().getWorld(worldKey);
                if (level != null) {
                    new GlobalPos(level, entity.x, entity.y, entity.z, entity.yaw, entity.pitch)
                        .teleport(player);
                } else {
                    TextHelper.sendMessageByKey(player, "world.dimension.not_found", entity.level);
                }

                this.close();
                return;
            }
            /* shift + right click -> specialized settings */
            if (clickType.isRight && clickType.shift) {
                if (!canOperateOnThisEntity(player, entity)) {
                    TextHelper.sendActionBarByKey(player, "works.work.set.no_perm");
                    return;
                }
                entity.openSpecializedSettingsGui(player, gui);
                this.close();
                return;
            }
            /* right click -> general settings */
            if (clickType.isRight) {
                if (!canOperateOnThisEntity(player, entity)) {
                    TextHelper.sendActionBarByKey(player, "works.work.set.no_perm");
                    return;
                }
                entity.openGeneralSettingsGui(player, gui);
                this.close();
            }
        };
    }

    @Override
    protected boolean filterEntity(Work entity, @NotNull String keyword) {
        return false;
    }
}

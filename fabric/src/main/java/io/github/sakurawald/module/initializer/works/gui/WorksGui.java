package io.github.sakurawald.module.initializer.works.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.sakurawald.core.auxiliary.minecraft.GuiHelper;
import io.github.sakurawald.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.gui.PagedGui;
import io.github.sakurawald.core.structure.SpatialPose;
import io.github.sakurawald.module.initializer.works.WorksInitializer;
import io.github.sakurawald.module.initializer.works.structure.work.abst.Work;
import io.github.sakurawald.module.initializer.works.structure.work.impl.ProductionWork;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.List;


public class WorksGui extends PagedGui<Work> {

    public WorksGui(ServerPlayerEntity player, @NotNull List<Work> entities, int pageIndex) {
        super(null, player, TextHelper.getTextByKey(player, "works.list.title"), entities, pageIndex);

        getFooter().setSlot(3, GuiHelper.makeAddButton(player)
            .setName(TextHelper.getTextByKey(player, "works.list.add"))
            .setCallback(() -> new AddWorkGui(player).open())
        );
        getFooter().setSlot(4, GuiHelper.makeHelpButton(player)
            .setLore(TextHelper.getTextListByKey(player, "works.list.help.lore")));

        if (entities == WorksInitializer.works.model().works) {
            getFooter().setSlot(5,
                GuiHelper.makeLetterAButton(player)
                    .setName(TextHelper.getTextByKey(player, "works.list.my_works"))
                    .setCallback(() -> search(player.getGameProfile().getName()).open())
            );
        } else {
            getFooter().setSlot(5,
                GuiHelper.makeHeartButton(player)
                    .setName(TextHelper.getTextByKey(player, "works.list.all_works"))
                    .setCallback(() -> new WorksGui(player, WorksInitializer.works.model().works, 0).open())
            );
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean hasPermission(@NotNull ServerPlayerEntity player, @NotNull Work work) {
        return player.getGameProfile().getName().equals(work.creator) || player.hasPermissionLevel(4);
    }

    @Override
    protected PagedGui<Work> make(@Nullable SimpleGui parent, ServerPlayerEntity player, Text title, @NotNull List<Work> entities, int pageIndex) {
        return new WorksGui(player, entities, pageIndex);
    }

    @Override
    protected GuiElementInterface toGuiElement(@NotNull Work entity) {
        ServerPlayerEntity player = getPlayer();
        return new GuiElementBuilder()
            .setItem(entity.getIconItem())
            .setName(TextHelper.getTextByValue(null, entity.name))
            .setLore(entity.asLore(player))
            .setCallback((index, clickType, actionType) -> {
                /* left click -> visit */
                if (clickType.isLeft) {
                    RegistryKey<World> worldKey = RegistryKey.of(RegistryKeys.WORLD, Identifier.of(entity.level));
                    ServerWorld level = ServerHelper.getServer().getWorld(worldKey);
                    if (level != null) {
                        new SpatialPose(level, entity.x, entity.y, entity.z, entity.yaw, entity.pitch)
                            .teleport(player);
                    } else {
                        TextHelper.sendMessageByKey(player, "world.dimension.not_found", entity.level);
                    }

                    this.close();
                    return;
                }
                /* shift + right click -> specialized settings */
                if (clickType.isRight && clickType.shift) {
                    if (!hasPermission(player, entity)) {
                        TextHelper.sendActionBarByKey(player, "works.work.set.no_perm");
                        return;
                    }
                    entity.openSpecializedSettingsGui(player, gui);
                    this.close();
                    return;
                }
                /* right click -> general settings */
                if (clickType.isRight) {
                    // check permission
                    if (!hasPermission(player, entity)) {
                        TextHelper.sendActionBarByKey(player, "works.work.set.no_perm");
                        return;
                    }
                    entity.openGeneralSettingsGui(player, gui);
                    this.close();
                }
            }).build();
    }

    @Override
    @NotNull
    protected List<Work> filter(@NotNull String keyword) {
        return this.getEntities().stream().filter(w ->
            w.creator.contains(keyword)
                || w.name.contains(keyword)
                || w.introduction != null && w.introduction.contains(keyword)
                || w.level.contains(keyword)
                || w.getIconItemIdentifier().contains(keyword)
                || w instanceof ProductionWork pw && pw.sample.sampleCounter != null && pw.sample.sampleCounter.keySet().stream().anyMatch(k -> k.contains(keyword))
        ).toList();
    }
}

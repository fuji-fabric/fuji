package mod.fuji.module.initializer.deathlog.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import mod.fuji.core.auxiliary.minecraft.GuiHelper;
import mod.fuji.core.auxiliary.minecraft.NbtHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.gui.component.gui.PagedGui;
import mod.fuji.core.gui.structure.GuiElementIR;
import mod.fuji.module.initializer.deathlog.DeathLogInitializer;
import mod.fuji.module.initializer.deathlog.structure.DeathNode;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DeathDataListGui extends PagedGui<String> {

    public DeathDataListGui(ServerPlayer player, @NotNull List<String> entities, int pageIndex) {
        super(null, player, TextHelper.getTextByKey(player, "deathlog.death_data.list.gui.title"), entities, pageIndex);
    }

    public static boolean hasDeathData(ServerPlayer player, CompoundTag root, String deadPlayerName) {
        if (root == null || root.isEmpty()) {
            TextHelper.sendTextByKey(player, "deathlog.death_data.empty", deadPlayerName);
            return false;
        }

        return true;
    }

    @Override
    protected @NotNull PagedGui<String> makePage(@Nullable SimpleGui parent, @NotNull ServerPlayer player, Component title, @NotNull List<String> entities, int pageIndex) {
        return new DeathDataListGui(player, entities, pageIndex);
    }

    @Override
    protected @NotNull GuiElementIR toGuiElement(@NotNull String entity) {
        GuiElementBuilder builder = GuiHelper.Button
            .makeLuckyBlockButton()
            .setName(Component.literal(entity))
            .setCallback(() -> openDeathNodeListGui(entity));

        return GuiElementIR.of(builder.build());
    }

    @Override
    protected void drawPagedGui() {
        super.drawPagedGui();
        GuiHelper.PlayerSkull.fillPlayerHeadTextures(this);
    }

    @SneakyThrows(IOException.class)
    private void openDeathNodeListGui(String entity) {
        NbtHelper.Storage.withNbtFile(DeathLogInitializer.getDeathDataPath(entity), root -> {
            /* Check if it has death nodes. */
            if (!hasDeathData(getPlayer(), root, entity)) {
                return;
            }

            /* Read death node list. */
            ListTag deathNodeList = NbtHelper.Walker.getOrCreateNbtElement(root, DeathNode.DEATHS_KEY, new ListTag());
            List<DeathNode> entries = deathNodeList.stream()
                .map(it -> DeathNode.fromNbt((CompoundTag) it))
                .collect(Collectors.toList());
            Collections.reverse(entries);
            new DeathNodeListGui(getBackendGui(), getPlayer(), entity, entries, 0)
                .open();
        });
    }

}

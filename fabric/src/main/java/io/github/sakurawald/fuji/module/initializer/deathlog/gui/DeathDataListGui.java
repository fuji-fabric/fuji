package io.github.sakurawald.fuji.module.initializer.deathlog.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.GuiHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.NbtHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.gui.component.gui.PagedGui;
import io.github.sakurawald.fuji.module.initializer.deathlog.DeathLogInitializer;
import io.github.sakurawald.fuji.module.initializer.deathlog.structure.DeathNode;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DeathDataListGui extends PagedGui<String> {

    public DeathDataListGui(ServerPlayerEntity player, @NotNull List<String> entities, int pageIndex) {
        super(null, player, TextHelper.getTextByKey(player, "deathlog.death_data.list.gui.title"), entities, pageIndex);
    }

    public static boolean hasDeathData(ServerPlayerEntity player, NbtCompound root, String deadPlayerName) {
        if (root == null || root.isEmpty()) {
            TextHelper.sendTextByKey(player, "deathlog.death_data.empty", deadPlayerName);
            return false;
        }

        return true;
    }

    @Override
    protected PagedGui<String> makePage(@Nullable SimpleGui parent, @NotNull ServerPlayerEntity player, Text title, @NotNull List<String> entities, int pageIndex) {
        return new DeathDataListGui(player, entities, pageIndex);
    }

    @Override
    protected @NotNull GuiElementInterface toGuiElement(@NotNull String entity) {
        GuiElementBuilder builder = GuiHelper.Button
            .makeLuckyBlockButton()
            .setName(Text.literal(entity))
            .setCallback(() -> openDeathNodeListGui(entity));

        return builder.build();
    }

    @Override
    protected void drawPagedGui() {
        super.drawPagedGui();
        GuiHelper.PlayerSkull.fillPlayerHeadTextures(this);
    }

    @SneakyThrows
    private void openDeathNodeListGui(String entity) {
        NbtHelper.Storage.withNbtFile(DeathLogInitializer.getDeathDataPath(entity), root -> {
            /* Check if it has death nodes. */
            if (!hasDeathData(getPlayer(), root, entity)) {
                close();
                return;
            }

            /* Read death node list. */
            NbtList deathNodeList = NbtHelper.Walker.getOrCreateNbtElement(root, DeathNode.DEATHS_KEY, new NbtList());
            List<DeathNode> entries = deathNodeList.stream()
                .map(it -> DeathNode.fromNbt((NbtCompound) it))
                .collect(Collectors.toList());
            Collections.reverse(entries);
            new DeathNodeListGui(getBackendGui(), getPlayer(), entity, entries, 0)
                .open();
        });
    }

}

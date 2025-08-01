package io.github.sakurawald.fuji.module.initializer.top_chunks.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.RegistryHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.gui.impl.gui.PagedGui;
import io.github.sakurawald.fuji.core.service.type_formatter.TypeFormatter;
import io.github.sakurawald.fuji.module.initializer.top_chunks.service.TopChunksService;
import io.github.sakurawald.fuji.module.initializer.top_chunks.structure.ChunkScore;
import net.minecraft.item.Items;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TopChunksGui extends PagedGui<ChunkScore> {

    public TopChunksGui(ServerPlayerEntity player, @NotNull List<ChunkScore> entities, int pageIndex) {
        super(null, player, TextHelper.getTextByKey(player, "top_chunks.list.gui.title"),
            TopChunksService.trimChunkScoreList(entities), pageIndex);
    }

    @Override
    protected PagedGui<ChunkScore> make(@Nullable SimpleGui parent, ServerPlayerEntity player, Text title, @NotNull List<ChunkScore> entities, int pageIndex) {
        return new TopChunksGui(player, entities, pageIndex);
    }

    @Override
    protected GuiElementInterface toGuiElement(ChunkScore entity) {
        ServerCommandSource commandSource = getPlayer().getCommandSource();

        List<Text> lore = new ArrayList<>();
        lore.add(TextHelper.getTextByKey(getPlayer(), "top_chunks.prop.dimension", RegistryHelper.getIdAsString(entity.getDimension())));
        lore.add(entity.computeChunkLocationText(commandSource));
        lore.add(TextHelper.getTextByKey(getPlayer(), "top_chunks.prop.players", entity.getPlayers()));
        lore.add(TypeFormatter.formatTypes(commandSource, entity.getType2amount()));

        ServerPlayerEntity player = getPlayer();
        if (ChunkScore.canClickToTeleportToThisChunk(player)) {
            lore.add(TextHelper.TEXT_EMPTY);
            lore.add(TextHelper.getTextByKey(player,"prompt.click.teleport"));
        }

        Text scoreText = TextHelper.getTextByKey(getPlayer(), "top_chunks.prop.score", entity.getScore());
        return new GuiElementBuilder()
            .setItem(entity.getPlayers().isEmpty() ? Items.WHITE_STAINED_GLASS : Items.LIME_STAINED_GLASS)
            .setName(scoreText)
            .setLore(lore)
            .setCallback(()-> {
                /* Click to teleport the player to the chunk. */
                if (!ChunkScore.canClickToTeleportToThisChunk(player)) return;
                entity.teleportToThisChunk(player);
                close();
            })
            .build();
    }

}

package mod.fuji.module.initializer.top_chunks.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import mod.fuji.core.auxiliary.minecraft.RegistryHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.gui.component.gui.PagedGui;
import mod.fuji.core.service.type_formatter.TypeFormatter;
import mod.fuji.module.initializer.top_chunks.service.TopChunksService;
import mod.fuji.module.initializer.top_chunks.structure.ChunkScore;
import net.minecraft.world.item.Items;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TopChunksGui extends PagedGui<ChunkScore> {

    public TopChunksGui(ServerPlayer player, @NotNull List<ChunkScore> entities, int pageIndex) {
        super(null, player, TextHelper.getTextByKey(player, "top_chunks.list.gui.title"),
            TopChunksService.trimChunkScoreList(entities), pageIndex);
    }

    @Override
    protected @NotNull PagedGui<ChunkScore> makePage(@Nullable SimpleGui parent, @NotNull ServerPlayer player, Component title, @NotNull List<ChunkScore> entities, int pageIndex) {
        return new TopChunksGui(player, entities, pageIndex);
    }

    @Override
    protected @NotNull GuiElementInterface toGuiElement(@NotNull ChunkScore entity) {
        CommandSourceStack commandSource = getPlayer().createCommandSourceStack();

        List<Component> lore = new ArrayList<>();
        lore.add(TextHelper.getTextByKey(getPlayer(), "top_chunks.prop.dimension", RegistryHelper.getIdAsString(entity.getDimension())));
        lore.add(entity.computeChunkLocationText(commandSource));
        lore.add(TextHelper.getTextByKey(getPlayer(), "top_chunks.prop.players", entity.getPlayers()));
        lore.add(TypeFormatter.formatTypes(commandSource, entity.getType2amount()));

        ServerPlayer player = getPlayer();
        if (ChunkScore.canClickToTeleportToThisChunk(player)) {
            lore.add(TextHelper.TEXT_EMPTY);
            lore.add(TextHelper.getTextByKey(player,"prompt.click.teleport"));
        }

        Component scoreText = TextHelper.getTextByKey(getPlayer(), "top_chunks.prop.score", entity.getScore());
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

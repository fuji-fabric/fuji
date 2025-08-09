package io.github.sakurawald.fuji.module.initializer.top_chunks;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.top_chunks.config.model.TopChunksConfigModel;
import io.github.sakurawald.fuji.module.initializer.top_chunks.gui.TopChunksGui;
import io.github.sakurawald.fuji.module.initializer.top_chunks.service.TopChunksService;
import io.github.sakurawald.fuji.module.initializer.top_chunks.structure.ChunkScore;
import java.util.PriorityQueue;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;


@Document(id = 1751826535209L, value = """
    Analyze all loaded chunks of the server, and find the most lagged chunks.
    """)
@ColorBox(id = 1751981000562L, color = ColorBox.ColorBoxTypes.NOTE, value = """
    ◉ How it works?
    This module use a `simple statistical method` to estimate the `degree of lag of a chunk`.
    The method is simple, it simply counts the `entities` and `block entities` inside a `chunk`.
    And sum up the `score` by the `type` of `entity` or `block entity`.

    It's simple, fast and useful.
    You can define the score of `a zombie` as `4`.
    The score of `a bee` as `15`.
    And the score of `a piston` as `10`.
    That depends on your case.
    Simple method often works.
    """)
public class TopChunksInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<TopChunksConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, TopChunksConfigModel.class);

    @Document(id = 1753056668984L, value = "An alias command to `/chunks message`.")
    @CommandNode("chunks")
    private static int $chunks(@CommandSource ServerCommandSource source) {
        return $message(source);
    }

    @Document(id = 1751826537195L, value = "List all chunks ordered by lag score, and send in `message`.")
    @CommandNode("chunks message")
    private static int $message(@CommandSource ServerCommandSource source) {
        PriorityQueue<ChunkScore> PQ = TopChunksService.computeChunkScores(source);
        var config1 = config.model();

        MutableText reportText = Text.empty();
        outer:
        for (int j = 0; j < config1.top.rows; j++) {
            for (int i = 0; i < config1.top.columns; i++) {
                if (PQ.isEmpty()) break outer;
                reportText
                    .append(PQ.poll().toText(source))
                    .append(TextHelper.TEXT_SPACE);
            }
            reportText.append(TextHelper.TEXT_NEWLINE);
        }
        source.sendMessage(reportText);
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1753056643919L, value = "List all chunks ordered by lag score, and send in `GUI`.")
    @CommandNode("chunks gui")
    private static int $gui(@CommandSource ServerCommandSource source) {
        PriorityQueue<ChunkScore> PQ = TopChunksService.computeChunkScores(source);
        new TopChunksGui(source.getPlayer(), PQ.stream().toList(), 0)
            .open();
        return CommandHelper.Return.SUCCESS;
    }

}

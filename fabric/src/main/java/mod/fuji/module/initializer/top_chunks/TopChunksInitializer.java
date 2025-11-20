package mod.fuji.module.initializer.top_chunks;

import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.core.config.handler.abst.BaseConfigurationHandler;
import mod.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import mod.fuji.core.document.annotation.ColorBox;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.module.initializer.ModuleInitializer;
import mod.fuji.module.initializer.top_chunks.config.model.TopChunksConfigModel;
import mod.fuji.module.initializer.top_chunks.gui.TopChunksGui;
import mod.fuji.module.initializer.top_chunks.service.TopChunksService;
import mod.fuji.module.initializer.top_chunks.structure.ChunkScore;
import java.util.PriorityQueue;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;


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

    public static final BaseConfigurationHandler<TopChunksConfigModel> config = ObjectConfigurationHandler.ofModule(BaseConfigurationHandler.CONFIG_JSON_LITERAL, TopChunksConfigModel.class);

    @Document(id = 1753056668984L, value = "An alias command to `/chunks message`.")
    @CommandNode("chunks")
    private static int $chunks(@CommandSource CommandSourceStack source) {
        return $message(source);
    }

    @Document(id = 1751826537195L, value = "List all chunks ordered by lag score, and send in `message`.")
    @CommandNode("chunks message")
    private static int $message(@CommandSource CommandSourceStack source) {
        PriorityQueue<ChunkScore> PQ = TopChunksService.computeChunkScores(source);
        var config1 = config.model();

        MutableComponent reportText = Component.empty();
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
        TextHelper.sendMessageByText(source, reportText);
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1753056643919L, value = "List all chunks ordered by lag score, and send in `GUI`.")
    @CommandNode("chunks gui")
    private static int $gui(@CommandSource CommandSourceStack source) {
        PriorityQueue<ChunkScore> PQ = TopChunksService.computeChunkScores(source);
        new TopChunksGui(source.getPlayer(), PQ.stream().toList(), 0)
            .open();
        return CommandHelper.Return.SUCCESS;
    }

}

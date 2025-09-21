package mod.fuji.module.initializer.leaderboard.structure;

import lombok.Value;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

@Value
public class LeaderBoardArgumentsParseResult {

    @Nullable Text errorText;
    LeaderBoardDescriptor leaderBoardDescriptor;
    Integer rankN;
    LeaderBoardTimeWindow timeWindow;

}

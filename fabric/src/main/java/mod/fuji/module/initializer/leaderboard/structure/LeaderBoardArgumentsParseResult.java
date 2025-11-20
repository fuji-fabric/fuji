package mod.fuji.module.initializer.leaderboard.structure;

import lombok.Value;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

@Value
public class LeaderBoardArgumentsParseResult {

    @Nullable Component errorText;
    LeaderBoardDescriptor leaderBoardDescriptor;
    Integer rankN;
    LeaderBoardTimeWindow timeWindow;

}

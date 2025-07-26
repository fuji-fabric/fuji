package io.github.sakurawald.fuji.module.initializer.leaderboard.structure;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

@Data
@AllArgsConstructor
public class LeaderBoardArgumentsParseResult {

    @Nullable Text errorText;
    LeaderBoardDescriptor leaderBoardDescriptor;
    Integer rankN;
    LeaderBoardTimeWindow timeWindow;

}

package io.github.sakurawald.fuji.module.initializer.command_meta.when_online.structure;

import io.github.sakurawald.fuji.core.document.annotation.Document;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Nullable;

@Data
@NoArgsConstructor
public class WhenOnlineTicket {

    public long createdTimestamp;
    @Document(id = 1751824011538L, value = "Who created this ticket?")
    public String creatorName;
    public String targetPlayer;
    public String command;
    @Document(id = 1751824017011L, value = "When the command is executed? (If not executed, the time is null)")
    public @Nullable Long executedTimestamp;

    public static WhenOnlineTicket make(String creatorName, String targetPlayer, String command) {
        WhenOnlineTicket whenOnlineTicket = new WhenOnlineTicket();
        whenOnlineTicket.createdTimestamp = System.currentTimeMillis();
        whenOnlineTicket.creatorName = creatorName;
        whenOnlineTicket.targetPlayer = targetPlayer;
        whenOnlineTicket.command = command;
        whenOnlineTicket.executedTimestamp = null;
        return whenOnlineTicket;
    }

    public boolean isExecuted() {
        return this.executedTimestamp != null;
    }
}

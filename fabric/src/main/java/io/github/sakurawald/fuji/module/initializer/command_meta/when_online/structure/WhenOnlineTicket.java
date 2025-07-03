package io.github.sakurawald.fuji.module.initializer.command_meta.when_online.structure;

import io.github.sakurawald.fuji.core.document.annotation.Document;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

@Data
public class WhenOnlineTicket {

    public long createdTimestamp;
    @Document("Who created this ticket?")
    public String creatorName;
    public String targetPlayer;
    public String command;
    @Document("When the command is executed? (If not executed, the time is null)")
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

}

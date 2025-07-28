package io.github.sakurawald.fuji.module.initializer.jail.structure;

import io.github.sakurawald.fuji.core.document.annotation.Document;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JailRecord {

    @Document(id = 1753685321191L, value = "The player name who created this `jail record`.")
    String creatorName;
    long createdTimestamp;

    @Document(id = 1753685346716L, value = "The `jailed player name`.")
    String playerName;
    int specifiedJailSeconds;
    int remainingJailSeconds;
    String reason;

    public static JailRecord make(String creatorName, String playerName, int jailSeconds, String reason) {
        JailRecord jailRecord = new JailRecord();
        jailRecord.setCreatorName(creatorName);
        jailRecord.setCreatedTimestamp(System.currentTimeMillis());
        jailRecord.setPlayerName(playerName);
        jailRecord.setSpecifiedJailSeconds(jailSeconds);
        jailRecord.setRemainingJailSeconds(jailSeconds);
        jailRecord.setReason(reason);

        return jailRecord;
    }

}

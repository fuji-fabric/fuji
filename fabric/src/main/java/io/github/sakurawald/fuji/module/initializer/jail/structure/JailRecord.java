package io.github.sakurawald.fuji.module.initializer.jail.structure;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.service.date_parser.DateParser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JailRecord {

    boolean enable;

    @Document(id = 1753685346716L, value = "The `jailed player name`.")
    String playerName;

    @Document(id = 1753685321191L, value = "The player name who created this `jail record`.")
    String creatorName;
    long createdTimestamp;

    int specifiedJailSeconds;
    int remainingJailSeconds;

    String reason;

    @ToString.Exclude transient JailDescriptor ownerJailDescriptor;

    public static JailRecord make(String creatorName, String playerName, int jailSeconds, String reason) {
        JailRecord jailRecord = new JailRecord();
        jailRecord.setEnable(true);
        jailRecord.setCreatorName(creatorName);
        jailRecord.setCreatedTimestamp(System.currentTimeMillis());
        jailRecord.setPlayerName(playerName);
        jailRecord.setSpecifiedJailSeconds(jailSeconds);
        jailRecord.setRemainingJailSeconds(jailSeconds);
        jailRecord.setReason(reason);

        return jailRecord;
    }

    public String getSpecifiedJailDuration() {
        return DateParser.formatAccumulatedSeconds(this.getSpecifiedJailSeconds());
    }

    public String getRemainingJailDuration() {
        return DateParser.formatAccumulatedSeconds(this.getRemainingJailSeconds());
    }

    public void onUpdateRecord(int passedTimeInMillSeconds) {
        if (this.getOwnerJailDescriptor().isCountRemainingJailSecondsWhenOffline()) {
            countRemainingJailSeconds(passedTimeInMillSeconds);
        } else {
            if (ServerHelper.isPlayerOnline(this.playerName)) {
                countRemainingJailSeconds(passedTimeInMillSeconds);
            }
        }
    }

    private void countRemainingJailSeconds(int passedTimeInMillSeconds) {
        int newValue = this.getRemainingJailSeconds() - (passedTimeInMillSeconds / 1000);
        newValue = Math.max(newValue, 0);
        this.setRemainingJailSeconds(newValue);
    }

}

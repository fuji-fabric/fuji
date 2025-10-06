package mod.fuji.module.initializer.command_warmup.structure;

import com.google.gson.annotations.SerializedName;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.service.bossbar.structure.Interruptible;
import mod.fuji.core.structure.Tags;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommandWarmupNode {

    Tags tags = Tags.makeDefault();

    @Document(id = 1751826877229L, value = """
        The `target command` and `warmup time in ms`.
        """)
    Command command;

    Interruptible interruptible;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Command {
        @Document(id = 1751826879068L, value = """
            The `regex` expression used to match the `target command`.
            """)
        String regex;

        @Document(id = 1751826881411L, value = """
            The `warmup time` in ms.
            """)
        @SerializedName(value = "warmup_time_ms", alternate = "ms")
        int warmupTimeMs;
    }

    public static CommandWarmupNode make(Command command, Interruptible interruptible) {
        return new CommandWarmupNode(Tags.makeDefault(), command, interruptible);
    }

}

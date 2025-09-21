package mod.fuji.checker;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.bugpatterns.BugChecker;
import java.util.List;

@AutoService(BugChecker.class)
@BugPattern(
    summary = """
        Directly calls to sendMessage() methods breaks the functionality of `--silent` and `--stdout` global optional arguments.
        Use the wrapped methods in TextHelper instead.
        """,
    severity = BugPattern.SeverityLevel.ERROR
)
public class BanDirectSendMessageMethodCall extends BanMethodCall {

    @Override
    public List<String> bannedMethodQualifiedNames() {
        return List.of("net.minecraft.server.command.ServerCommandSource.sendMessage"
            , "net.minecraft.server.network.ServerPlayerEntity.sendMessage"
            , "net.minecraft.entity.player.PlayerEntity.sendMessage"
            , "net.minecraft.server.command.CommandOutput.sendMessage");
    }

    @Override
    public List<String> ignoreClassQualifiedNamePrefixes() {
        return List.of("mod.fuji.module.initializer.tester.TesterInitializer"
                , "mod.fuji.module.initializer.tester.functions.TestFunctions"
                , "mod.fuji.core.auxiliary.minecraft.TextHelper"
                , "mod.fuji.core.command.descriptor.CommandDescriptor"
                , "mod.fuji.core.command.assistant.CommandAssistant");
    }
}

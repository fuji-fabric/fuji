package tests.text;

import auxiliary.JavaParserUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.descriptor.CommandDescriptor;
import io.github.sakurawald.fuji.module.initializer.tester.TesterInitializer;
import io.github.sakurawald.fuji.module.initializer.tester.functions.TestFunctions;
import java.util.List;
import org.junit.jupiter.api.Test;

public class TextTest {

//    @Test
//    void banTheDirectCallToVanillaMinecraftTextSendingMethods() {
//        JavaParserUtil.banMethodCalls(
//            List.of("net.minecraft.server.command.ServerCommandSource.sendMessage"
//                , "net.minecraft.server.network.ServerPlayerEntity.sendMessage"
//                , "net.minecraft.entity.player.PlayerEntity.sendMessage"
//                , "net.minecraft.server.command.CommandOutput.sendMessage")
//        , List.of(TesterInitializer.class, TestFunctions.class, TextHelper.class, CommandDescriptor.class)
//        , """
//                Directly calls to sendMessage() methods breaks the functionality of `--silent` and `--stdout` global optional arguments.
//                Use the wrapped methods in TextHelper instead.
//                """);
//    }

}

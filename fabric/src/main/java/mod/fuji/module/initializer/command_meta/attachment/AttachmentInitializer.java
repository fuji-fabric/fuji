package mod.fuji.module.initializer.command_meta.attachment;

import com.mojang.brigadier.context.CommandContext;
import mod.fuji.core.document.annotation.ColorBox;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandRequirement;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.core.command.argument.wrapper.impl.GreedyString;
import mod.fuji.core.manager.Managers;
import mod.fuji.module.initializer.ModuleInitializer;
import mod.fuji.module.initializer.command_meta.attachment.command.argument.wrapper.SubjectId;
import mod.fuji.module.initializer.command_meta.attachment.command.argument.wrapper.SubjectName;
import lombok.SneakyThrows;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.io.IOException;

@Document(id = 1751824793427L, value = """
    Provides a unified attachment facility, to attach data to any object.
    """)
@ColorBox(id = 1751970220438L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ Set a attachment.
    Issue: `/attachment set news today hello world`

    ◉ Get a attachment.
    Issue: `/attachment get news today`
    """)



@CommandNode("attachment")
@CommandRequirement(level = 4)
public class AttachmentInitializer extends ModuleInitializer {

    @CommandNode("set")
    @SneakyThrows(IOException.class)
    private static int $set(@CommandSource CommandContext<ServerCommandSource> ctx, SubjectName subject, SubjectId uuid, GreedyString data) {
        Managers.getAttachmentManager().setAttachment(subject.getValue(), uuid.getValue(), data.getValue());
        return CommandHelper.Return.SUCCESS;
    }

    @SneakyThrows(IOException.class)
    @CommandNode("unset")
    private static int $unset(@CommandSource CommandContext<ServerCommandSource> ctx, SubjectName subject, SubjectId uuid) {
        boolean flag = Managers.getAttachmentManager().unsetAttachment(subject.getValue(), uuid.getValue());
        TextHelper.sendTextByKey(ctx.getSource(), flag ? "operation.success" : "operation.fail");
        return CommandHelper.Return.SUCCESS;
    }

    @SneakyThrows(IOException.class)
    @CommandNode("get")
    private static int $get(@CommandSource CommandContext<ServerCommandSource> ctx, SubjectName subject, SubjectId uuid) {
        String attachment = Managers.getAttachmentManager().getAttachment(subject.getValue(), uuid.getValue());

        TextHelper.sendMessageByText(ctx.getSource(), Text.literal(attachment));
        return CommandHelper.Return.SUCCESS;
    }
}

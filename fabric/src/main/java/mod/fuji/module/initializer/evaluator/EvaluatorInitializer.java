package mod.fuji.module.initializer.evaluator;

import java.util.List;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandRequirement;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.core.command.argument.wrapper.impl.GreedyString;
import mod.fuji.core.document.annotation.ColorBox;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.module.initializer.ModuleInitializer;
import mod.fuji.module.initializer.evaluator.formatter.PrettyFormatter;
import mod.fuji.module.initializer.evaluator.Reader.LispReader;
import mod.fuji.module.initializer.evaluator.Reader.token.Token;
import net.minecraft.server.command.ServerCommandSource;

@Document(id = 1758985259601L, value = """
    This module provides an `evaluator` for `Lisp dialect language`.
    """)
@ColorBox(id = 1758985675914L, color = ColorBox.ColorBoxTypes.WARNING, value = """
    ◉ This module is currently an experimental module.
    Changed may be made in the future versions.
    """)
public class EvaluatorInitializer extends ModuleInitializer {

    @CommandNode("lisp eval")
    @CommandRequirement(level = 4)
    private static int $eval(@CommandSource ServerCommandSource source, GreedyString form) {
        final String $form = form.getValue();
        LispReader lispReader = new LispReader($form);
        List<Token> AST = lispReader.read();
        PrettyFormatter.prettyPrint(AST);

        return CommandHelper.Return.SUCCESS;
    }

}

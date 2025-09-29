package mod.fuji.module.initializer.evaluator;

import java.util.List;
import mod.fuji.core.auxiliary.LogUtil;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandRequirement;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.core.command.argument.wrapper.impl.GreedyString;
import mod.fuji.core.document.annotation.ColorBox;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.module.initializer.ModuleInitializer;
import mod.fuji.module.initializer.evaluator.evaluator.compiler.LispCompiler;
import mod.fuji.module.initializer.evaluator.evaluator.compiler.formatter.LispNodeFormatter;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispNode;
import mod.fuji.module.initializer.evaluator.formatter.PrettyFormatter;
import mod.fuji.module.initializer.evaluator.reader.LispReader;
import mod.fuji.module.initializer.evaluator.reader.token.Token;
import net.minecraft.server.command.ServerCommandSource;

@Document(id = 1758985259601L, value = """
    This module provides an `evaluator` for `Lisp dialect language`.
    """)
@ColorBox(id = 1758985675914L, color = ColorBox.ColorBoxTypes.WARNING, value = """
    ◉ This module is currently an experimental module.
    Changes may be made in the future versions.
    """)
public class EvaluatorInitializer extends ModuleInitializer {

    @CommandNode("lisp eval")
    @CommandRequirement(level = 4)
    private static int $eval(@CommandSource ServerCommandSource source, GreedyString form) {
        final String $form = form.getValue();
        LispReader lispReader = new LispReader($form);
        List<Token> tokenStream = lispReader.read();
        PrettyFormatter.prettyPrint(tokenStream);

        LispCompiler lispCompiler = new LispCompiler(tokenStream);
        LispNode AST = lispCompiler.compile();

        LogUtil.warn("""
            AST Print =

            {}
            """, AST);

        LogUtil.warn("""

            AST Pretty Print = {}
            """, LispNodeFormatter.prettyPrint(AST));


        return CommandHelper.Return.SUCCESS;
    }

}

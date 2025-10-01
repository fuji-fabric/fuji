package mod.fuji.module.initializer.evaluator;

import java.util.List;
import mod.fuji.core.auxiliary.LogUtil;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandRequirement;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.core.command.argument.wrapper.impl.GreedyString;
import mod.fuji.core.document.annotation.ColorBox;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.module.initializer.ModuleInitializer;
import mod.fuji.module.initializer.evaluator.evaluator.LispEvaluator;
import mod.fuji.module.initializer.evaluator.evaluator.compiler.LispCompiler;
import mod.fuji.module.initializer.evaluator.evaluator.compiler.formatter.LispObjectFormatter;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispList;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispObject;
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

    @Document(id = 1759152497031L, value = "Alias to `/lisp eval` command.")
    @CommandNode("lisp")
    @CommandRequirement(level = 4)
    private static int $root(@CommandSource ServerCommandSource source, GreedyString form) {
        return $eval(source, form);
    }

    @Document(id = 1759152513020L, value = "Eval a lisp form.")
    @CommandNode("lisp eval")
    @CommandRequirement(level = 4)
    private static int $eval(@CommandSource ServerCommandSource source, GreedyString form) {
        try {
            final String $form = form.getValue();
            LispReader lispReader = new LispReader($form);
            List<Token> tokenStream = lispReader.read();
            PrettyFormatter.prettyPrint(tokenStream);

            LispCompiler lispCompiler = new LispCompiler(tokenStream);
            LispList AST = lispCompiler.compile();

            LogUtil.debug("""
                AST Print =
                {}
                """, AST);

            LogUtil.debug("""
                AST Pretty Print =
                {}
                """, LispObjectFormatter.prettyPrint(AST));

            LispEvaluator lispEvaluator = new LispEvaluator(AST);
            LispObject eval = lispEvaluator.eval();
            LogUtil.debug("""
                eval = {}
                """, eval);

            /* Print the value. */
            TextHelper.sendTextByKey(source, "lisp.eval.value", TextHelper.Parsers.escapeTags(eval.toString()));
            return CommandHelper.Return.SUCCESS;
        } catch (Exception e) {
            TextHelper.sendTextByKey(source, "lisp.eval.error", TextHelper.Parsers.escapeTags(e.toString()));
            return CommandHelper.Return.FAILURE;
        }
    }

}

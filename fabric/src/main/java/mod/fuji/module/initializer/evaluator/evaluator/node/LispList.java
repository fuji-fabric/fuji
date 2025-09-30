package mod.fuji.module.initializer.evaluator.evaluator.node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import mod.fuji.module.initializer.evaluator.evaluator.compiler.exception.LispCompilationException;
import mod.fuji.module.initializer.evaluator.evaluator.context.Environment;
import org.jetbrains.annotations.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
public class LispList extends LispObject {

    final List<LispObject> nodes;

    private LispList(@NotNull List<LispObject> nodes) {
        this.nodes = nodes;
    }

    public static @NotNull LispList of() {
        return new LispList(new ArrayList<>());
    }

    public static @NotNull LispList of(LispObject... nodes) {
        List<LispObject> list = Arrays.stream(nodes).toList();
        return new LispList(list);
    }

    public static @NotNull LispList of(@NotNull List<LispObject> nodes) {
        return new LispList(nodes);
    }

    @SuppressWarnings("SequencedCollectionMethodCanBeUsed")
    @Override
    public @NotNull LispObject eval(@NotNull Environment environment) {
        /* An empty list is treated as nil value. */
        if (this.nodes.isEmpty()) {
            return Environment.NIL;
        }

        /* The first component of a function call must be a LispSymbol. */
        LispObject first = this.nodes.get(0);
        if (!(first instanceof LispSymbol functionNameSymbol)) {
            throw new LispCompilationException("Illegal function call.");
        }

        /* Eval the function arguments. */
        List<LispObject> args = new ArrayList<>();
        for (int i = 1; i < this.nodes.size(); i++) {
            LispObject arg = this.nodes.get(i).eval(environment);
            args.add(arg);
        }
//        Environment childEnvironment = new Environment();

        /* Get the function value. */
        LispFunction functionValue = environment
            .lookupSymbol(functionNameSymbol.getName())
            .getFunctionValue()
            .orElseThrow(() -> new LispCompilationException("The function %s is undefined.".formatted(functionNameSymbol.getName())));

        /* Eval the function with arguments.*/
        LispObject value = functionValue.call(environment, args);
        return value;
    }

}

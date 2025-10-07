package mod.fuji.evaluator.evaluator.node.function.lambda.parameter;

import java.util.Optional;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import mod.fuji.evaluator.evaluator.node.LispList;
import mod.fuji.evaluator.evaluator.node.LispSymbol;
import org.jetbrains.annotations.NotNull;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class OptionalParameterSpecifier extends ParameterSpecifier {

    final Optional<LispList> initForm;
    final Optional<LispSymbol> suppliedVar;

    private OptionalParameterSpecifier(@NotNull String parameterName, Optional<LispList> initForm, Optional<LispSymbol> suppliedVar) {
        super(parameterName);
        this.initForm = initForm;
        this.suppliedVar = suppliedVar;
    }

    public static @NotNull ParameterSpecifier of(@NotNull String parameterName, Optional<LispList> initForm, Optional<LispSymbol> suppliedVar) {
        return new OptionalParameterSpecifier(parameterName, initForm, suppliedVar);
    }
}

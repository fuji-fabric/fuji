package mod.fuji.evaluator.evaluator.value.function.lambda.parameter;

import java.util.Optional;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import mod.fuji.evaluator.evaluator.value.LispList;
import mod.fuji.evaluator.evaluator.value.LispSymbol;
import org.jetbrains.annotations.NotNull;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class KeyParameterSpecifier extends ParameterSpecifier {

    final Optional<LispList> initForm;
    final Optional<LispSymbol> suppliedVar;

    private KeyParameterSpecifier(@NotNull String parameterName, @NotNull Optional<LispList> initForm, @NotNull Optional<LispSymbol> suppliedVar) {
        super(":" + parameterName);
        this.initForm = initForm;
        this.suppliedVar = suppliedVar;
    }

    public static @NotNull ParameterSpecifier of(@NotNull String parameterName, Optional<LispList> initForm, Optional<LispSymbol> suppliedVar) {
        return new KeyParameterSpecifier(parameterName, initForm, suppliedVar);
    }

}

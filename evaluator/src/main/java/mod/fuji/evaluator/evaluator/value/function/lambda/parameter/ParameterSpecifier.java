package mod.fuji.evaluator.evaluator.value.function.lambda.parameter;


import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public abstract class ParameterSpecifier {

    @NotNull String parameterName;

}

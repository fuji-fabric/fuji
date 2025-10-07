package mod.fuji.evaluator.evaluator.structure.lambda.parameter;


import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public abstract class ParameterSpecifier {

    @NotNull String parameterName;

}

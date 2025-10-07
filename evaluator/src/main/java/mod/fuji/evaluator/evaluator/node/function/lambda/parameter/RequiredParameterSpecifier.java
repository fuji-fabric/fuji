package mod.fuji.evaluator.evaluator.node.function.lambda.parameter;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class RequiredParameterSpecifier extends ParameterSpecifier {

    private RequiredParameterSpecifier(@NotNull String parameterName) {
        super(parameterName);
    }

    public static @NotNull RequiredParameterSpecifier of(@NotNull String parameterName) {
        return new RequiredParameterSpecifier(parameterName);
    }

}

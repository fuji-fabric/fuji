package mod.fuji.evaluator.evaluator.structure.lambda.parameter;

import org.jetbrains.annotations.NotNull;

public class RestParameterSpecifier extends ParameterSpecifier {
    private RestParameterSpecifier(@NotNull String parameterName) {
        super(parameterName);
    }

    public static @NotNull RestParameterSpecifier of(@NotNull String parameterName) {
        return new RestParameterSpecifier(parameterName);
    }

}

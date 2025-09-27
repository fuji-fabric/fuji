package mod.fuji.module.initializer.evaluator.parser.token;

import lombok.Value;
import mod.fuji.module.initializer.evaluator.parser.structure.StringRange;
import org.jetbrains.annotations.NotNull;

@Value(staticConstructor = "of")
public class Token {

    @NotNull TokenType tokenType;
    @NotNull StringRange stringRange;
    @NotNull String stringText;

}

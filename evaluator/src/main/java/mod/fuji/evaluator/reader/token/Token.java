package mod.fuji.evaluator.reader.token;

import lombok.Value;
import mod.fuji.evaluator.reader.structure.StringRange;
import org.jetbrains.annotations.NotNull;

@Value(staticConstructor = "of")
public class Token {

    @NotNull TokenType tokenType;
    @NotNull StringRange tokenRange;

    /**
 * A string text here must be a substring of the original input text. (Literal Representation)
 **/
    @NotNull String tokenContent;

}

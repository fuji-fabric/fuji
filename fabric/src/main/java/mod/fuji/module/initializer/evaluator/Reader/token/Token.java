package mod.fuji.module.initializer.evaluator.Reader.token;

import lombok.Value;
import mod.fuji.core.document.annotation.ForDeveloper;
import mod.fuji.module.initializer.evaluator.Reader.structure.StringRange;
import org.jetbrains.annotations.NotNull;

@Value(staticConstructor = "of")
public class Token {

    @NotNull TokenType tokenType;
    @NotNull StringRange tokenRange;

    @ForDeveloper("A string text here must be a substring of the original input text. (Literal Representation)")
    @NotNull String tokenContent;

}

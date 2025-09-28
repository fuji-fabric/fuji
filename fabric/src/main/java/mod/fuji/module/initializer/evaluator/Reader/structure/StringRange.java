package mod.fuji.module.initializer.evaluator.Reader.structure;

import lombok.Value;

@Value(staticConstructor = "of")
public class StringRange {
    int start;
    int end;
}

package auxiliary.structure;

import io.github.classgraph.AnnotationInfo;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.MethodInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Data
@AllArgsConstructor
public class ExtendedAnnotationInfo {
    @NotNull AnnotationInfo annotationInfo;
    @NotNull ClassInfo declaringClass;
    @Nullable MethodInfo declaringMethod;

    public ExtendedAnnotationInfo(@NotNull AnnotationInfo annotationInfo, @NotNull ClassInfo declaringClass) {
        this.annotationInfo = annotationInfo;
        this.declaringClass = declaringClass;
    }

}

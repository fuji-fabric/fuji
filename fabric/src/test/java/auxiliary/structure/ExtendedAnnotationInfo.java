package auxiliary.structure;

import io.github.classgraph.AnnotationInfo;
import io.github.classgraph.ClassInfo;
import lombok.Data;

@Data
public class ExtendedAnnotationInfo {
    final AnnotationInfo annotationInfo;
    final ClassInfo declaringClass;
}

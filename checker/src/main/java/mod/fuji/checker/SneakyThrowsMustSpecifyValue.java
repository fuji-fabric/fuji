package mod.fuji.checker;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.MethodTree;
import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.code.Symbol;

import java.util.List;

@AutoService(com.google.errorprone.bugpatterns.BugChecker.class)
@BugPattern(
    summary = "@SneakyThrows must explicitly specify target exception types in its value attribute.",
    explanation = """
        Lombok's @SneakyThrows defaults to Throwable.class if no value is provided.
        To enforce clarity and avoid overly broad exception declarations, this checker requires
        that all @SneakyThrows usages specify explicit exception classes.
        """,
    severity = BugPattern.SeverityLevel.ERROR
)
public class SneakyThrowsMustSpecifyValue extends BugChecker implements MethodTreeMatcher {

    private static final String SNEAKY_THROWS = "lombok.SneakyThrows";

    @Override
    public Description matchMethod(MethodTree tree, VisitorState state) {
        Symbol.MethodSymbol methodSymbol = ASTHelpers.getSymbol(tree);
        if (methodSymbol == null) {
            return Description.NO_MATCH;
        }

        for (Attribute.Compound annotation : methodSymbol.getAnnotationMirrors()) {
            if (annotation.type.tsym.getQualifiedName().contentEquals(SNEAKY_THROWS)) {
                // Look for the "value" element in the annotation
                List<Attribute> values = annotation.getElementValues().values().stream().toList();

                if (values.isEmpty()) {
                    // Means no value attribute was specified
                    return buildDescription(tree)
                            .setMessage("@SneakyThrows must declare target exception types, e.g., @SneakyThrows(IOException.class).")
                            .build();
                }
            }
        }

        return Description.NO_MATCH;
    }
}

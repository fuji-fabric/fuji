package mod.fuji.checker;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.matchers.Description;
import com.sun.source.tree.ClassTree;

import mod.fuji.auxiliary.CheckerUtil;
import java.util.List;

@AutoService(BugChecker.class)
@BugPattern(
    summary = """
        The @Data annotation should be used with @NoArgsConstructor annotation:
        1. The @Data annotation only provides the @RequiredArgsConstructor annotation.
        2. If you have a final field in the class, then it will generates a required args constructor for that class.
        In this case, the implicit no args constructor is removed.
        3. Many other Java libraries require a no args constructor to make instance, like `Gson` and `Quartz`.
        Lacking a default no args constructor will introduce un-expected behaviours.
        """,
    severity = BugPattern.SeverityLevel.ERROR
)
public class ConfigModelClassAnnotationsConvention extends BugChecker implements BugChecker.ClassTreeMatcher {

    @Override
    public Description matchClass(ClassTree tree, VisitorState state) {
        List<String> annotations = CheckerUtil.getAnnotationQualifiedNames(tree);

        /* Only apply this checker for config model classes. */
        if (!CheckerUtil.getEnclosingClassQualifiedName(state).contains(".model.")) {
            return Description.NO_MATCH;
        }

        if (annotations.contains("lombok.Data")
            && !annotations.contains("lombok.NoArgsConstructor")) {
            return buildDescription(tree).build();
        }
        return Description.NO_MATCH;
    }

}

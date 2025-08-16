package io.github.sakurawald.checker;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.matchers.Description;
import com.google.googlejavaformat.Op;
import com.sun.source.tree.MethodTree;

import io.github.sakurawald.auxiliary.CheckerUtil;
import java.util.Optional;

@AutoService(BugChecker.class)
@BugPattern(
    summary = "Methods annotated with @CommandNode should have names starting with '$'.",
    severity = BugPattern.SeverityLevel.ERROR
)
@SuppressWarnings({"MemoizeConstantVisitorStateLookups", "ReturnValueIgnored"})
public class CommandActionMethodNamingConvention extends BugChecker implements BugChecker.MethodTreeMatcher {

    @Override
    public Description matchMethod(MethodTree tree, VisitorState state) {
        if (CheckerUtil.getAnnotationQualifiedNames(tree).contains("io.github.sakurawald.fuji.core.command.annotation.CommandNode")) {
            if (!CheckerUtil.getSimpleName(tree).startsWith("$")) {
                return buildDescription(tree).build();
            }
        }
        return Description.NO_MATCH;
    }

}

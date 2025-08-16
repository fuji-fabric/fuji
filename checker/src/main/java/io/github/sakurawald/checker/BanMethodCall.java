package io.github.sakurawald.checker;

import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.tools.javac.code.Symbol;
import java.util.List;

public class BanMethodCall extends BugChecker implements BugChecker.MethodInvocationTreeMatcher {

    final List<String> bannedMethodQualifiedNames;
    final List<String> ignoreClassQualifiedNames;

    public BanMethodCall(List<String> bannedMethodQualifiedNames, List<String> ignoreClassQualifiedNames) {
        this.bannedMethodQualifiedNames = bannedMethodQualifiedNames;
        this.ignoreClassQualifiedNames = ignoreClassQualifiedNames;
    }

    @Override
    public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
        Symbol.MethodSymbol methodSymbol = ASTHelpers.getSymbol(tree);
        Symbol.ClassSymbol classSymbol = methodSymbol.enclClass();

        String className = classSymbol.toString();
        String methodSimpleName = methodSymbol.getSimpleName().toString();

        if (ignoreClassQualifiedNames.contains(className)) {
            return Description.NO_MATCH;
        }

        String methodName = className + "." + methodSimpleName;
        if (bannedMethodQualifiedNames.contains(methodName)) {
            return buildDescription(tree).build();
        }

        return Description.NO_MATCH;
    }
}

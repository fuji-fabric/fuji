package io.github.sakurawald.checker;

import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.tools.javac.code.Symbol;
import io.github.sakurawald.auxiliary.CheckerUtil;
import java.util.List;
import lombok.Getter;

public abstract class BanMethodCall extends BugChecker implements BugChecker.MethodInvocationTreeMatcher {

    @Getter(lazy = true)
    private final List<String> bannedMethodQualifiedNames = bannedMethodQualifiedNames();

    @Getter(lazy = true)
    private final List<String> ignoreClassQualifiedNamePrefixes = ignoreClassQualifiedNamePrefixes();

    public abstract List<String> bannedMethodQualifiedNames();

    public abstract List<String> ignoreClassQualifiedNamePrefixes();

    @Override
    public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
        /* Ignore the method calls in specified classes. */
        String enclosingClassQualifiedName = CheckerUtil.getEnclosingClassQualifiedName(state);
        if (ignoreClassQualifiedNamePrefixes().stream().anyMatch(enclosingClassQualifiedName::startsWith)) {
            return Description.NO_MATCH;
        }

        /* Match the method calls. */
        Symbol.MethodSymbol invokingMethodSymbol = ASTHelpers.getSymbol(tree);
        Symbol.ClassSymbol declaringClassSymbol = invokingMethodSymbol.enclClass();

        String declaringClassQualifiedName = declaringClassSymbol.toString();
        String invokingMethodSimpleName = invokingMethodSymbol.getSimpleName().toString();

        String methodName = declaringClassQualifiedName + "." + invokingMethodSimpleName;
        if (bannedMethodQualifiedNames().contains(methodName)) {
            return buildDescription(tree).build();
        }

        return Description.NO_MATCH;
    }

}

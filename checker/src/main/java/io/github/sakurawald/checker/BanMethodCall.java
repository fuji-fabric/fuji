package io.github.sakurawald.checker;

import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.code.Symbol;
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
        Symbol.ClassSymbol enclosingClassSymbol = findEnclosingClass(state);
        String enclosingClassQualifiedName = enclosingClassSymbol.toString();
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
            System.out.println("enclosing class = " + enclosingClassQualifiedName);
            return buildDescription(tree).build();
        }

        return Description.NO_MATCH;
    }

    private static Symbol.ClassSymbol findEnclosingClass(VisitorState state) {
        TreePath classPath = state.getPath();
        while (classPath != null && !(classPath.getLeaf() instanceof ClassTree)) {
            classPath = classPath.getParentPath();
        }

        if (classPath != null) {
            return (Symbol.ClassSymbol) ASTHelpers.getSymbol(classPath.getLeaf());
        }
        throw new RuntimeException("Could not find enclosing class");
    }
}

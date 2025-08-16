package io.github.sakurawald.auxiliary;

import com.google.errorprone.VisitorState;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ClassTree;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.code.Symbol;
import java.util.List;

public class CheckerUtil {

    public static Symbol.ClassSymbol findEnclosingClass(VisitorState state) {
        TreePath classPath = state.getPath();
        while (classPath != null && !(classPath.getLeaf() instanceof ClassTree)) {
            classPath = classPath.getParentPath();
        }

        if (classPath != null) {
            return (Symbol.ClassSymbol) ASTHelpers.getSymbol(classPath.getLeaf());
        }
        throw new RuntimeException("Could not find enclosing class");
    }

    public static String getEnclosingClassQualifiedName(VisitorState state) {
        Symbol.ClassSymbol enclosingClassSymbol = findEnclosingClass(state);
        return enclosingClassSymbol.toString();
    }

    private static List<Symbol> getAnnotationSymbols(ClassTree tree) {
        return tree.getModifiers().getAnnotations()
            .stream()
            .map(annotation -> ASTHelpers.getSymbol(annotation.getAnnotationType()))
            .toList();
    }

    public static List<String> getAnnotationQualifiedNames(ClassTree tree) {
            return getAnnotationSymbols(tree)
                .stream()
                .map(it -> it.getQualifiedName().toString())
                .toList();
    }
}

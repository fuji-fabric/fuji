package io.github.sakurawald.auxiliary;

import com.google.errorprone.VisitorState;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.code.Symbol;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class CheckerUtil {

    public static @NotNull Symbol.ClassSymbol findEnclosingClass(@NotNull VisitorState state) {
        TreePath classPath = state.getPath();
        while (classPath != null && !(classPath.getLeaf() instanceof ClassTree)) {
            classPath = classPath.getParentPath();
        }

        if (classPath != null) {
            return (Symbol.ClassSymbol) ASTHelpers.getSymbol(classPath.getLeaf());
        }
        throw new RuntimeException("Could not find enclosing class");
    }

    public static @NotNull String getEnclosingClassQualifiedName(@NotNull VisitorState state) {
        Symbol.ClassSymbol enclosingClassSymbol = findEnclosingClass(state);
        return enclosingClassSymbol.toString();
    }

    public static @NotNull String getQualifiedName(@NotNull ClassTree classTree) {
        return ASTHelpers.getSymbol(classTree).getQualifiedName().toString();
    }

    private static @NotNull List<Symbol> getAnnotationSymbols(@NotNull ClassTree tree) {
        return tree.getModifiers().getAnnotations()
            .stream()
            .map(annotationTree -> ASTHelpers.getSymbol(annotationTree.getAnnotationType()))
            .toList();
    }

    public static @NotNull List<String> getAnnotationQualifiedNames(@NotNull ClassTree tree) {
            return getAnnotationSymbols(tree)
                .stream()
                .map(it -> it.getQualifiedName().toString())
                .toList();
    }

    public static @NotNull List<Symbol> getAnnotationSymbols(@NotNull MethodTree tree) {
        return tree.getModifiers().getAnnotations()
            .stream()
            .map(annotationTree -> ASTHelpers.getSymbol(annotationTree.getAnnotationType()))
            .toList();
    }

    public static @NotNull List<String> getAnnotationQualifiedNames(@NotNull MethodTree tree) {
        return getAnnotationSymbols(tree)
            .stream()
            .map(it -> it.getQualifiedName().toString())
            .toList();
    }

    public static @NotNull String getSimpleName(@NotNull MethodTree tree) {
        return tree.getName().toString();
    }

}

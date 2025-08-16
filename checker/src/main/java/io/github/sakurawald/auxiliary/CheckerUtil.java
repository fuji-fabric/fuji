package io.github.sakurawald.auxiliary;

import com.google.errorprone.VisitorState;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    public static class Types {

        private static final Map<String, Type> KNOWN_TYPES = new HashMap<>();

        @SuppressWarnings({"ReturnValueIgnored", "CodeBlock2Expr"})
        public static void ensureTypeExists(@NotNull VisitorState state, @NotNull String typeString) {
            KNOWN_TYPES.computeIfAbsent(typeString, key -> {
                return Optional
                    .ofNullable(state.getTypeFromString(typeString))
                    .map(value -> KNOWN_TYPES.put(key, value))
                    .orElseThrow(() -> new RuntimeException("Unknown type %s, did you refactor this type before?".formatted(typeString)));
            });
        }

    }
}

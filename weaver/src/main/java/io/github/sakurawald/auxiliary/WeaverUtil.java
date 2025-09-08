package io.github.sakurawald.auxiliary;

import com.google.errorprone.annotations.CheckReturnValue;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeCopier;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Names;
import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import javax.annotation.processing.FilerException;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import org.jetbrains.annotations.NotNull;

@CheckReturnValue
public class WeaverUtil {

    public static final String MIXIN_CLASS_FQCN = "org.spongepowered.asm.mixin.Mixin";
    public static final String MIXIN_PRIORITY_PARAMETER_NAME = "priority";

    // Magic Value: 61553
    public static final int TOKEN_PLACEHOLDER = 0xF071;

    public static @NotNull String toSimpleName(@NotNull String FQCN) {
        int index = FQCN.lastIndexOf('.');
        return index >= 0 ? FQCN.substring(index + 1) : FQCN;
    }

    public static boolean matchAnnotationTreeByClass(@NotNull JCTree.JCAnnotation annotationTree, @NotNull Class<? extends Annotation> annotationClass) {
        String FQCN = annotationClass.getName();
        return matchAnnotationTreeByName(annotationTree, FQCN);
    }

    public static boolean matchAnnotationTreeByName(@NotNull JCTree.JCAnnotation annotationTree, @NotNull String FQCN) {
        String simpleName = toSimpleName(FQCN);

        String annotationTypeString = annotationTree.annotationType.toString();
        return annotationTypeString.equals(FQCN)
            || annotationTypeString.equals(simpleName)
            || annotationTypeString.endsWith("." + simpleName);
    }

    public static boolean matchExpressionTreeByName(@NotNull JCTree.JCExpression expressionTree, @NotNull String name) {
        return name.equals(expressionTree.toString());
    }

    public static @NotNull JCTree.JCModifiers removeAnnotation(@NotNull TreeMaker treeMaker, @NotNull JCTree.JCModifiers modifiersTree, @NotNull Class<? extends Annotation> annotationClass) {
        /* Check if there is any annotations in the modifiers. */
        List<JCTree.JCAnnotation> annotationTrees = modifiersTree.annotations;
        if (annotationTrees == null || annotationTrees.isEmpty()) {
            return modifiersTree;
        }

        /* Filter the specified annotation out. */
        ListBuffer<JCTree.JCAnnotation> collected = new ListBuffer<>();
        for (JCTree.JCAnnotation annotationTree : annotationTrees) {
            if (matchAnnotationTreeByClass(annotationTree, annotationClass)) {
                continue;
            }
            collected.append(annotationTree);
        }
        return treeMaker.Modifiers(modifiersTree.flags, collected.toList());
    }

    public static @NotNull JCTree.JCClassDecl deepCopy(@NotNull JCTree.JCClassDecl node, @NotNull TreeMaker treeMaker) {
        TreeCopier<Void> copier = new TreeCopier<>(treeMaker);
        return copier.copy(node, null);
    }

    /**
     * Emit a .java source file for the generated class so other processors/tooling can see it.
     */
    public static void emitJavaSourceFile(@NotNull JCTree.JCCompilationUnit cu, @NotNull ProcessingEnvironment processingEnv, @NotNull JCTree.JCClassDecl generatedClassTree) {
        StringBuilder javaTextBuilder = new StringBuilder();

        /* Append the package declaration expression. */
        String packageName = (cu.getPackageName() != null) ? cu.getPackageName().toString() : "";
        String classSimpleName = generatedClassTree.getSimpleName().toString();
        String classQualifiedName = packageName.isEmpty() ? classSimpleName : (packageName + "." + classSimpleName);

        if (!packageName.isEmpty()) {
            javaTextBuilder.append("package ").append(packageName).append(";\n\n");
        }

        /* Append import expressions from the compilation unit. */
        try {
            List<JCTree.JCImport> imports = cu.getImports();
            if (imports != null && !imports.isEmpty()) {
                for (JCTree.JCImport imp : imports) {
                    // JCImport.toString() should produce a valid import statement text
                    javaTextBuilder.append(imp.toString()).append("\n");
                }
                javaTextBuilder.append("\n");
            }
        } catch (Throwable t) {
            // Be resilient: if cu doesn't expose imports the usual way, skip imports
        }

        /* Append the generated class text */
        javaTextBuilder
            .append(generatedClassTree)
            .append("\n");

        /* Create the source file via the Filer */
        try {
            JavaFileObject javaFileObject = processingEnv.getFiler().createSourceFile(classQualifiedName);
            try (Writer w = javaFileObject.openWriter()) {
                w.write(javaTextBuilder.toString());
            }
        } catch (FilerException fe) {
            // File already created (common in repeated rounds); log as note and continue.
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Source file already exists for generated class: " + classQualifiedName);
        } catch (IOException ioe) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "Failed to write generated source for " + classQualifiedName + ": " + ioe);
        } catch (Throwable t) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "Unexpected error writing generated source for " + classQualifiedName + ": " + t);
        }
    }

    public static void removeClassTree(@NotNull JCTree.JCCompilationUnit cu, @NotNull JCTree.JCClassDecl classTree) {
        ListBuffer<JCTree> defs = new ListBuffer<>();
        for (var def : cu.defs) {
            if (def != classTree) {
                defs.add(def);
            }
        }
        cu.defs = defs.toList();
    }

    public static @NotNull JCTree.JCAnnotation setAnnotationParameterValue(@NotNull TreeMaker treeMaker, @NotNull Names names, @NotNull JCTree.JCAnnotation annotationTree, @NotNull String annotationParameterName, @NotNull Object annotationParameterValue) {
        /* Make the assigned tree. */
        JCTree.JCAssign assignTree = treeMaker.Assign(
            treeMaker.Ident(names.fromString(annotationParameterName)),
            treeMaker.Literal(annotationParameterValue)
        );

        /* Iterate the declared annotation args. */
        boolean replaced = false;
        List<JCTree.JCExpression> annotationArgs = List.nil();
        for (JCTree.JCExpression expressionTree : annotationTree.args) {
            if (expressionTree instanceof JCTree.JCAssign asg) {
                if (matchExpressionTreeByName(asg.lhs, annotationParameterName)) {
                    annotationArgs = annotationArgs.append(assignTree);
                    replaced = true;
                } else {
                    annotationArgs = annotationArgs.append(expressionTree);
                }
            } else {
                annotationArgs = annotationArgs.append(expressionTree);
            }
        }

        if (!replaced) {
            annotationArgs = annotationArgs.append(assignTree);
        }

        return treeMaker.Annotation(annotationTree.annotationType, annotationArgs);
    }

    public static @NotNull JCTree.JCModifiers setAnnotationParameterValue(@NotNull TreeMaker treeMaker,
                                                                          @NotNull Names names,
                                                                          @NotNull JCTree.JCModifiers modifiersTree,
                                                                          @NotNull String annotationFqcn,
                                                                          @NotNull String annotationParameterName,
                                                                          @NotNull Object annotationParameterValue) {

        List<JCTree.JCAnnotation> annotations = modifiersTree.annotations;

        List<JCTree.JCAnnotation> patchedAnnotations = List.nil();
        for (JCTree.JCAnnotation annotation : annotations) {
            if (matchAnnotationTreeByName(annotation, annotationFqcn)) {
                JCTree.JCAnnotation patchedAnnotationTree = setAnnotationParameterValue(treeMaker, names, annotation, annotationParameterName, annotationParameterValue);
                patchedAnnotations = patchedAnnotations.append(patchedAnnotationTree);
            } else {
                patchedAnnotations = patchedAnnotations.append(annotation);
            }
        }

        return treeMaker.Modifiers(modifiersTree.flags, patchedAnnotations);
    }

    public static @NotNull JCTree.JCCompilationUnit getCompilationUnit(@NotNull JavacTrees javacTrees, @NotNull Element element) {
        TreePath treePath = javacTrees.getPath(element);
        return (JCTree.JCCompilationUnit) treePath.getCompilationUnit();
    }

    public static void setAnnotationParameterValue(@NotNull TreeMaker treeMaker,
                                                   @NotNull Names names,
                                                   @NotNull JCTree.JCClassDecl classTree,
                                                   @NotNull String annotationFqcn,
                                                   @NotNull String annotationParameterName,
                                                   @NotNull Object annotationParameterValue) {
        classTree.accept(new TreeScanner() {

            @Override
            public void visitClassDef(JCTree.JCClassDecl clazz) {
                clazz.mods = setAnnotationParameterValue(treeMaker, names, clazz.mods,
                    annotationFqcn, annotationParameterName, annotationParameterValue);
                super.visitClassDef(clazz);
            }

            @Override
            public void visitMethodDef(JCTree.JCMethodDecl method) {
                method.mods = setAnnotationParameterValue(treeMaker, names, method.mods,
                    annotationFqcn, annotationParameterName, annotationParameterValue);
                super.visitMethodDef(method);
            }

            @Override
            public void visitVarDef(JCTree.JCVariableDecl var) {
                var.mods = setAnnotationParameterValue(treeMaker, names, var.mods,
                    annotationFqcn, annotationParameterName, annotationParameterValue);
                super.visitVarDef(var);
            }
        });
    }

    private static String getMethodName(@NotNull JCTree.JCMethodInvocation methodInvocationTree) {
        return methodInvocationTree.meth.toString();
    }

    public static void setMethodInvocationArgumentValue(@NotNull TreeMaker maker,
                                                        @NotNull JCTree.JCClassDecl classTree,
                                                        @NotNull String methodQualifiedName,
                                                        int methodArity,
                                                        int methodArgumentIndex,
                                                        @NotNull Object methodArgumentValue) {

        PatchValidator.withMinimalPatchCount(1, globalPatchCount -> {
            classTree.accept(new TreeScanner() {
                @Override
                public void visitApply(JCTree.JCMethodInvocation methodInvocationTree) {
                    String callee = getMethodName(methodInvocationTree);
                    if (callee.equals(methodQualifiedName) && methodInvocationTree.args.size() == methodArity) {

                        PatchValidator.withMinimalPatchCount(1, localPatchCount -> {
                            ListBuffer<JCTree.JCExpression> newArgs = new ListBuffer<>();
                            int index = 0;
                            for (JCTree.JCExpression arg : methodInvocationTree.args) {
                                if (index == methodArgumentIndex) {
                                    newArgs.add(maker.Literal(methodArgumentValue));

                                    localPatchCount.getAndIncrement();
                                    globalPatchCount.getAndIncrement();
                                } else {
                                    newArgs.add(arg);
                                }
                                index++;
                            }
                            methodInvocationTree.args = newArgs.toList();
                        });

                    }
                    super.visitApply(methodInvocationTree);
                }
            });

        });

    }


}

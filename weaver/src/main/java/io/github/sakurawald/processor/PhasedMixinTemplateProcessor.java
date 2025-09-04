package io.github.sakurawald.processor;

import com.google.auto.service.AutoService;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCAssign;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCModifiers;
import com.sun.tools.javac.tree.TreeCopier;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;
import io.github.sakurawald.annotation.PhasedMixinTemplate;
import java.util.Objects;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
public class PhasedMixinTemplateProcessor extends AbstractProcessor {

    private JavacTrees trees;
    private TreeMaker maker;
    private Names names;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        var jpe = (JavacProcessingEnvironment) processingEnv;
        Context ctx = jpe.getContext();
        this.trees = JavacTrees.instance(processingEnv);
        this.maker = TreeMaker.instance(ctx);
        this.names = Names.instance(ctx);
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of(PhasedMixinTemplate.class.getCanonicalName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element el : roundEnv.getElementsAnnotatedWith(PhasedMixinTemplate.class)) {
            /* Filter the interesting node. */
            if (!(el instanceof TypeElement)) continue;
            var anno = el.getAnnotation(PhasedMixinTemplate.class);
            if (anno == null) continue;

            var classTree = (JCClassDecl) trees.getTree(el);
            if (classTree == null) continue;

            /* Validate arrays match. */
            String[] mixinClassNameSuffixes = anno.suffixes();
            int[] mixinAnnotationPriorities = anno.priorities();
            if (mixinClassNameSuffixes.length != mixinAnnotationPriorities.length) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "@GeneratePhasedMixin: suffixes/priorities length mismatch", el);
                continue;
            }

            /* Get compilation unit for injection. */
            TreePath path = trees.getPath(el);
            var cu = (JCCompilationUnit) path.getCompilationUnit();
            for (int i = 0; i < mixinClassNameSuffixes.length; i++) {
                generateMixinVariant(cu, classTree, mixinClassNameSuffixes[i], mixinAnnotationPriorities[i], anno.mixinTypeFqcn(), anno.priorityElement());
            }

            /* Remove original annotated class from compilation unit. */
            ListBuffer<JCTree> defs = new ListBuffer<>();
            for (var def : cu.defs) {
                if (def != classTree) {
                    defs.add(def);
                }
            }
            cu.defs = defs.toList();
        }
        return false;
    }


    private void generateMixinVariant(JCCompilationUnit cu,
                                      JCClassDecl original,
                                      String classNameSuffix,
                                      int newPriority,
                                      String mixinFqcn,
                                      String priorityElementName) {

        /* Deep copy the class AST */
        JCClassDecl copy = deepCopy(original);

        /* Rename class name, with suffix. */
        @SuppressWarnings("UnnecessaryLocalVariable")
        Name newName = names.fromString(original.getSimpleName().toString() + classNameSuffix);
        copy.name = newName;

        /* Patch @Mixin annotation's 'priority' */
        copy.mods = patchMixinPriority(copy.mods, mixinFqcn, priorityElementName, newPriority);

        /* Make package-private visibility flag, to prevent the PUBLIC modifier in the same compilation unit. */
        copy.mods.flags &= ~com.sun.tools.javac.code.Flags.PUBLIC;

        /* Inject the getEventPriority() method. */
        JCAnnotation uniqueAnno = maker.Annotation(
            maker.Ident(names.fromString("Unique")),
            List.nil()
        );

        JCModifiers getterMods = maker.Modifiers(
            Flags.PRIVATE | Flags.STATIC,
            List.of(uniqueAnno)
        );

        JCExpression returnType = maker.TypeIdent(TypeTag.INT);
        Name methodName = names.fromString("getEventPriority");
        var returnStmt = maker.Return(maker.Literal(newPriority));
        var body = maker.Block(0, List.of(returnStmt));

        var methodDef = maker.MethodDef(
            getterMods,
            methodName,
            returnType,
            List.nil(),   // type parameters
            List.nil(),   // parameters
            List.nil(),   // throws
            body,
            null          // default value
        );

        copy.defs = copy.defs.append(methodDef);

        /* Inject the new class definition into the same compilation unit. */
        cu.defs = cu.defs.append(copy);
    }

    private JCClassDecl deepCopy(JCClassDecl node) {
        TreeCopier<Void> copier = new TreeCopier<>(maker);
        return copier.copy(node, null);
    }

    private JCModifiers patchMixinPriority(JCModifiers mods,
                                           String mixinFqcn,
                                           String priorityElementName,
                                           int newPriority) {
        List<JCAnnotation> annotations = mods.annotations;
        List<JCAnnotation> patched = List.nil();

        for (JCAnnotation ann : annotations) {
            if (annotationMatches(ann, mixinFqcn)) {
                patched = patched.append(patchPriorityArg(ann, priorityElementName, newPriority));
            } else {
                patched = patched.append(ann);
            }
        }
        return maker.Modifiers(mods.flags, patched);
    }


    private boolean annotationMatches(JCAnnotation ann, String fqcn) {
        String at = ann.annotationType.toString();
        return at.equals(fqcn) || at.endsWith("." + simpleName(fqcn)) || at.equals(simpleName(fqcn));
    }

    private String simpleName(String fqcn) {
        int idx = fqcn.lastIndexOf('.');
        return idx >= 0 ? fqcn.substring(idx + 1) : fqcn;
    }

    private JCAnnotation patchPriorityArg(JCAnnotation ann, String name, int value) {
        JCAssign assign = maker.Assign(
            maker.Ident(names.fromString(name)),
            maker.Literal(value)
        );

        boolean replaced = false;
        List<JCExpression> args = List.nil();

        for (JCExpression e : ann.args) {
            if (e instanceof JCAssign asg) {
                var lhs = asg.lhs.toString();
                if (Objects.equals(lhs, name)) {
                    args = args.append(assign);
                    replaced = true;
                } else {
                    args = args.append(e);
                }
            } else {
                args = args.append(e);
            }
        }

        if (!replaced) {
            args = args.append(assign);
        }

        return maker.Annotation(ann.annotationType, args);
    }
}

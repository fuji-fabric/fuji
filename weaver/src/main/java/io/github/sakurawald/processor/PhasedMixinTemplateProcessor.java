package io.github.sakurawald.processor;

import com.google.auto.service.AutoService;
import com.google.errorprone.annotations.Keep;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;
import io.github.sakurawald.annotation.PhasedMixinTemplate;
import io.github.sakurawald.auxiliary.WeaverUtil;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import org.jetbrains.annotations.NotNull;

@AutoService(Processor.class)
public class PhasedMixinTemplateProcessor extends AbstractProcessor {

    public static final String EVENT_PRODUCER_LITERAL = "EventProducer";
    public static final String EVENT_PRODUCER_INJECTOR_PRIORITY_PARAMETER_NAME = "injectorPriority";

    private JavacTrees javacTrees;
    private TreeMaker treeMaker;
    private Names names;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        var jpe = (JavacProcessingEnvironment) processingEnv;
        Context ctx = jpe.getContext();
        this.javacTrees = JavacTrees.instance(processingEnv);
        this.treeMaker = TreeMaker.instance(ctx);
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
        for (Element element : roundEnv.getElementsAnnotatedWith(PhasedMixinTemplate.class)) {
            /* Filter the interesting node. */
            if (!(element instanceof TypeElement)) continue;
            PhasedMixinTemplate targetAnnotation = element.getAnnotation(PhasedMixinTemplate.class);
            if (targetAnnotation == null) continue;

            var classTree = (JCClassDecl) javacTrees.getTree(element);
            if (classTree == null) continue;

            /* Validate arrays match. */
            String[] mixinClassNameSuffixes = targetAnnotation.suffixes();
            int[] mixinAnnotationPriorities = targetAnnotation.priorities();
            if (mixinClassNameSuffixes.length != mixinAnnotationPriorities.length) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "@GeneratePhasedMixin: suffixes/priorities length mismatch", element);
                continue;
            }

            /* Get compilation unit for injection. */
            JCCompilationUnit cu = WeaverUtil.getCompilationUnit(javacTrees, element);
            for (int i = 0; i < mixinClassNameSuffixes.length; i++) {
                JCClassDecl generatedClassTree = generatePhasedMixinClassTree(cu, classTree, mixinClassNameSuffixes[i], mixinAnnotationPriorities[i]);
                WeaverUtil.emitJavaSourceFile(cu, processingEnv, generatedClassTree);
            }

            /* Remove the template mixin class from current compilation unit. */
            WeaverUtil.removeClassTree(cu, classTree);
        }
        return false;
    }


    @SuppressWarnings({"JdkObsolete", "unused"})
    @Keep
    private JCClassDecl generatePhasedMixinClassTree(@NotNull JCCompilationUnit cu,
                                                     @NotNull JCClassDecl originalClassTree,
                                                     @NotNull String generatedClassNameSuffix,
                                                     int injectorPriorityValue) {

        /* Deep copy the class AST */
        JCClassDecl copyClassTree = WeaverUtil.deepCopy(originalClassTree, treeMaker);

        /* Rename class name, with suffix. */
        @SuppressWarnings("UnnecessaryLocalVariable")
        Name newName = names.fromString(originalClassTree.getSimpleName().toString() + generatedClassNameSuffix);
        copyClassTree.name = newName;

        /* Remove @PhasedMixinTemplate to prevent recursive processing. */
        copyClassTree.mods = WeaverUtil.removeAnnotation(this.treeMaker, copyClassTree.mods, PhasedMixinTemplate.class);

        /* Patch @Mixin annotation. */
        copyClassTree.mods = WeaverUtil.patchModifiersTree(treeMaker, names, copyClassTree.mods, WeaverUtil.MIXIN_CLASS_FQCN, WeaverUtil.MIXIN_PRIORITY_PARAMETER_NAME, injectorPriorityValue);

        /* Patch @EventProducer annotations. */
        WeaverUtil.patchAnnotationTreeRecursively(treeMaker, names, copyClassTree, EVENT_PRODUCER_LITERAL, EVENT_PRODUCER_INJECTOR_PRIORITY_PARAMETER_NAME, injectorPriorityValue);

        /* Patch the injector priority placeholder. */
        WeaverUtil.patchMethodInvocationTree(treeMaker, copyClassTree, "EventManager.dispatchEvent", 3, 2, injectorPriorityValue);

        return copyClassTree;
    }

}

package auxiliary;

//import com.github.javaparser.ParserConfiguration;
//import com.github.javaparser.StaticJavaParser;
//import com.github.javaparser.ast.CompilationUnit;
//import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
//import com.github.javaparser.ast.expr.MethodCallExpr;
//import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
//import com.github.javaparser.symbolsolver.JavaSymbolSolver;
//import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
//import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
//import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
//import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

public class JavaParserUtil {

//    public static List<CompilationUnit> cuList = makeCompilationUnits();
//
//    public static @NotNull List<CompilationUnit> getCompilationUnits() {
//        return cuList;
//    }
//
//    public static boolean sameFileName(@NotNull String fileName, @NotNull Class<?> className) {
//        return fileName.equals(className.getSimpleName() + ".java");
//    }
//
//    @SneakyThrows
//    private static @NotNull List<CompilationUnit> makeCompilationUnits() {
//        /* Make the type solver. */
//        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
//        combinedTypeSolver.add(new ReflectionTypeSolver());
//        combinedTypeSolver.add(new JavaParserTypeSolver(TestUtil.JAVA_PARSER_INPUT_DIRECTORY_PATH));
//        List<String> lines = Files.readAllLines(TestUtil.FABRIC_PROJECT_BUILD_PATH.resolve("collected-classpath.txt"));
//        for (String line : lines) {
//            combinedTypeSolver.add(new JarTypeSolver(Path.of(line)));
//        }
//        JavaSymbolSolver javaSymbolSolver = new JavaSymbolSolver(combinedTypeSolver);
//
//        /* Make the java parser. */
//        StaticJavaParser
//            .getParserConfiguration()
//            .setSymbolResolver(javaSymbolSolver)
//            .setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_16);
//
//        /* Parse the input files. */
//        List<CompilationUnit> cuList = new ArrayList<>();
//        try (Stream<Path> sourceFiles = Files
//            .walk(TestUtil.JAVA_PARSER_INPUT_DIRECTORY_PATH)
//            .filter(Files::isRegularFile)) {
//            sourceFiles.forEach(sourceFile -> {
//                try {
//                    LogUtil.disabled("Compile the source file: {}", sourceFile);
//                    CompilationUnit compilationUnit = StaticJavaParser.parse(sourceFile);
//                    cuList.add(compilationUnit);
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//            });
//        }
//        return cuList;
//    }
//
//    public static @NotNull String getFriendlyName(@NotNull ClassOrInterfaceDeclaration it) {
//        return it.getFullyQualifiedName().orElseGet(() -> it.getName().toString());
//    }
//
//    public static void banMethodCalls(@NotNull List<String> bannedQualifiedMethodNames, @NotNull List<Class<?>> ignoreClasses, @NotNull String reason) {
//        List<MethodCallExpr> failedMethodCalls = new ArrayList<>();
//        getCompilationUnits()
//            .forEach(cu -> cu
//                .findAll(MethodCallExpr.class)
//                .stream()
//                .filter(it -> {
//                    String fileName = cu.getStorage().orElseThrow().getFileName();
//                    return ignoreClasses
//                        .stream()
//                        .noneMatch(ignoreClass -> sameFileName(fileName, ignoreClass));
//                })
//                .filter(it -> bannedQualifiedMethodNames
//                    .stream()
//                    .anyMatch(bannedQualifiedMethodName -> bannedQualifiedMethodName.endsWith(it.getNameAsString())))
//                .forEach(it -> {
//                    ResolvedMethodDeclaration resolved = it.resolve();
//                    String qualifiedName = resolved.getQualifiedName();
//                    if (bannedQualifiedMethodNames
//                        .stream()
//                        .anyMatch(bannedQualifiedName -> bannedQualifiedName.equals(qualifiedName))) {
//                        String fileName = cu.getStorage().orElseThrow().getFileName();
//                        LogUtil.error("The call to {} is banned. (file = {}, expression = {})", qualifiedName, fileName, it);
//                        failedMethodCalls.add(it);
//                    }
//                }));
//
//        if (!failedMethodCalls.isEmpty()) {
//            throw new RuntimeException(reason);
//        }
//    }
}

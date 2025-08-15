package tests.config;

import auxiliary.TestUtil;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

public class ConfigurationTest {

    private static @NotNull String getFriendlyName(@NotNull ClassOrInterfaceDeclaration it) {
        return it.getFullyQualifiedName().orElseGet(() -> it.getName().toString());
    }

    @Test
    @SneakyThrows
    public void ensureDataAnnotationIsUsedWithNoArgsConstructorAnnotation() {
        JavaParser javaParser = new JavaParser();

        List<CompilationUnit> cuList = new ArrayList<>();
        try (Stream<Path> sourceFiles = Files
            .walk(TestUtil.FABRIC_PROJECT_MANIFOLD_DUMP_SOURCE_PATH)
            .filter(Files::isRegularFile)
            .filter(path -> path.toString().contains("model"))) {
            sourceFiles.forEach(sourceFile -> {
                try {
                    LogUtil.disabled("Compile the source file: {}", sourceFile);
                    ParseResult<CompilationUnit> parseResult = javaParser.parse(sourceFile);
                    CompilationUnit compilationUnit = parseResult
                        .getResult()
                        .orElseThrow(() -> {
                            parseResult.getProblems().forEach(System.out::println);
                            return new RuntimeException();
                        });
                    cuList.add(compilationUnit);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        List<ClassOrInterfaceDeclaration> failedClass = new ArrayList<>();

        cuList.forEach(cu -> {
            List<ClassOrInterfaceDeclaration> targets = cu.findAll(ClassOrInterfaceDeclaration.class, it -> it.getAnnotationByName("Data").isPresent()
                && it.getAnnotationByName("NoArgsConstructor").isEmpty());
            failedClass.addAll(targets);
        });

        failedClass.forEach(it -> LogUtil.error("Failed class = {}", getFriendlyName(it)));
        if (!failedClass.isEmpty()) {
            throw new RuntimeException("The @Data annotation should be used with @NoArgsConstructor annotation.");
        }
    }
}

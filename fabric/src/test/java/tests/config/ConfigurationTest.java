package tests.config;

import auxiliary.JavaParserUtil;
//import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

public class ConfigurationTest {

//    @Test
//    @SneakyThrows
//    void ensureDataAnnotationIsUsedWithNoArgsConstructorAnnotation() {
//        List<ClassOrInterfaceDeclaration> failedClass = new ArrayList<>();
//        JavaParserUtil.getCompilationUnits()
//            .stream()
//            .filter(cu -> cu.getStorage().orElseThrow().getPath().toString().contains(FileSystems.getDefault().getSeparator() + "model" + FileSystems.getDefault().getSeparator()))
//            .forEach(cu -> {
//            List<ClassOrInterfaceDeclaration> targets = cu.findAll(ClassOrInterfaceDeclaration.class, it -> it.getAnnotationByName("Data").isPresent()
//                && it.getAnnotationByName("NoArgsConstructor").isEmpty());
//            failedClass.addAll(targets);
//        });
//
//        failedClass.forEach(it -> LogUtil.error("Failed class = {}", JavaParserUtil.getFriendlyName(it)));
//        if (!failedClass.isEmpty()) {
//            throw new RuntimeException("""
//                The @Data annotation should be used with @NoArgsConstructor annotation:
//                1. The @Data annotation only provides the @RequiredArgsConstructor annotation.
//                2. If you have a final field in the class, then it will generates a required args constructor for that class.
//                In this case, the implicit no args constructor is removed.
//                3. Many other Java libraries require a no args constructor to make instance, like `Gson` and `Quartz`.
//                Lacking a default no args constructor will introduce un-expected behaviours.
//                """);
//        }
//    }

}

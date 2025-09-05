package tests.reflection;

import auxiliary.classgraph.ClassGraphUtil;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled
public class ReflectionTest {

    public static final Set<String> REQUIRED_TYPE_DECLARATIONS = new HashSet<>() {
        {
            this.add("io.github.sakurawald.fuji.core.command.annotation.CommandNode");
            this.add("io.github.sakurawald.fuji.core.event.annotation.EventConsumer");
            this.add("io.github.sakurawald.fuji.core.event.annotation.EventProducer");
        }
    };

    @Test
    void ensureTypesDeclared() {
        try (ScanResult scanResult = ClassGraphUtil
            .makeBaseClassGraph()
            .enableAllInfo()
            .scan()) {

            Set<String> allDeclaredTypeQualifiedNames = scanResult
                .getAllClasses()
                .stream()
                .map(ClassInfo::getName)
                .collect(Collectors.toSet());

            REQUIRED_TYPE_DECLARATIONS.removeAll(allDeclaredTypeQualifiedNames);
            if (!REQUIRED_TYPE_DECLARATIONS.isEmpty()) {
                LogUtil.error("""
                The following types should be declared: {}

                Ensures a type with a given qualified name exists in the source.
                This check is typically used for types that are used in reflection.
                To ensure the naming consistent after the refactoring.
                """, REQUIRED_TYPE_DECLARATIONS);
                throw new RuntimeException();
            }
        }
    }
}

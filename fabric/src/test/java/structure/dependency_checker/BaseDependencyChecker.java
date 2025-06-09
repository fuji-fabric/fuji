package structure.dependency_checker;

import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class BaseDependencyChecker {

    private @NotNull List<Dependency> reduce(@NotNull List<Dependency> deps) {
        // merge
        Map<String, Dependency> map = new HashMap<>();
        for (Dependency dep : deps) {
            map.putIfAbsent(dep.definition, dep);
            map.get(dep.definition).getReference().addAll(dep.reference);
        }

        //reduce
        for (Dependency dep : map.values()) {
            Set<String> set = new HashSet<>(dep.reference);
            dep.reference.clear();
            dep.reference.addAll(set);
        }

        return map.values().stream().toList();
    }

    public abstract Dependency makeDependency(Path file);

    @SneakyThrows(IOException.class)
    public List<Dependency> makeDependencies(Path dir) {
        /* make */
        List<Dependency> dependencies = new ArrayList<>();

        Files.walkFileTree(dir, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) {
                Dependency dependency = makeDependency(path);
                if (dependency != null) {
                    dependencies.add(dependency);
                }

                return FileVisitResult.CONTINUE;
            }
        });

        /* reduce */
        return this.reduce(dependencies);
    }
}

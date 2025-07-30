package tests.dependency.structure;

import java.util.Comparator;
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

public abstract class BaseDependencyChecker {

    private @NotNull List<DependencyNode> simplify(@NotNull List<DependencyNode> nodes) {
        /* Group the nodes into single definition name, and merge their reference names. */
        Map<String, DependencyNode> groupedNodes = new HashMap<>();
        for (DependencyNode node : nodes) {
            groupedNodes.putIfAbsent(node.definition, node);
            groupedNodes.get(node.definition).reference.addAll(node.reference);
        }

        /* Remove duplicated reference names. */
        for (DependencyNode groupedNode : groupedNodes.values()) {
            groupedNode.reference = new ArrayList<>(new HashSet<>(groupedNode.reference));
        }

        /* Sort the reference names. */
        groupedNodes.values().forEach(node -> node.reference.sort(Comparator.naturalOrder()));

        return groupedNodes.values().stream().toList();
    }

    public abstract @NotNull DependencyNode makeDependencyNode(Path file);

    @SneakyThrows(IOException.class)
    public @NotNull List<DependencyNode> makeDependencyNodes(Path dir) {
        /* Walk the file directory to make node for each file. */
        List<DependencyNode> dependencyNodes = new ArrayList<>();
        Files.walkFileTree(dir, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) {
                if (path.toString().contains("lombok.config")) {
                    return FileVisitResult.CONTINUE;
                }

                DependencyNode node = makeDependencyNode(path);
                /* Should we ignore this dependency node? */
                if (DependencyNode.IGNORE_THIS_DEPENDENCY_NODE.equals(node)) {
                    return FileVisitResult.CONTINUE;
                }

                /* Collect this dependency node. */
                dependencyNodes.add(node);
                return FileVisitResult.CONTINUE;
            }
        });

        /* Simplify and return the nodes. */
        return this.simplify(dependencyNodes);
    }
}

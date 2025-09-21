package tests.dependency.structure;

import mod.fuji.core.auxiliary.LogUtil;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DependencyNode {
    public static final DependencyNode IGNORE_THIS_DEPENDENCY_NODE = new DependencyNode(null, null);

    public String definition;
    public List<String> reference;

    public static void tryReportViolationDependencyNodes(List<DependencyNode> dependencyNodes, String reason) {
        if (!dependencyNodes.isEmpty()) {
            LogUtil.warn("===== The following dependency nodes violates the rule =====");
            dependencyNodes.forEach(System.out::println);
            throw new RuntimeException(reason);
        }
    }

    public void includeReference(String... prefixes) {
        this.reference = this.reference
            .stream()
            .filter(referenceName -> Arrays
                .stream(prefixes)
                .anyMatch(referenceName::startsWith))
            .collect(Collectors.toList());
    }

    public void excludeReference(String... prefixes) {
        this.reference = this.reference
            .stream()
            .filter(referenceName -> Arrays
                .stream(prefixes)
                .noneMatch(referenceName::startsWith))
            .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder
            .append("= Dependency Node =").append("\n")
            .append("[Definition] %s".formatted(this.definition)).append("\n");

        if (!this.reference.isEmpty()) {
            builder.append("[Reference] ").append("\n");
            this.reference
                .forEach(it -> builder.append(" - ").append(it).append("\n"));
        }

        return builder.toString();
    }
}

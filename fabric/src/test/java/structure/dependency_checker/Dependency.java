package structure.dependency_checker;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.function.Predicate;

@Data
@AllArgsConstructor
public class Dependency {
    String definition;
    List<String> reference;

    public void filterReference(String... prefixes) {
        this.reference = this.reference.stream().filter(className -> {
            for (String prefix : prefixes) {
                if (className.startsWith(prefix)) {
                    return true;
                }
            }
            return false;
        }).toList();
    }

    public void filterReference(Predicate<String> predicate) {
        this.reference = this.reference.stream().filter(predicate).toList();
    }

    public void excludeReference(String... prefixes) {
        this.reference = this.reference.stream().filter(className -> {
            for (String prefix : prefixes) {
                if (className.startsWith(prefix)) {
                    return false;
                }
            }
            return true;
        }).toList();
    }


    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("= Symbol =").append("\n")
            .append("[Definition] %s".formatted(this.definition)).append("\n");

        if (!this.reference.isEmpty()) {
            builder.append("[Reference] ").append("\n");
            this.reference.forEach(it -> builder.append(" - ").append(it).append("\n"));
        }

        return builder.toString();
    }
}

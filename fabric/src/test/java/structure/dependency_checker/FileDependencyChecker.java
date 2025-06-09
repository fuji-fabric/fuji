package structure.dependency_checker;

import auxiliary.TestUtility;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class FileDependencyChecker extends BaseDependencyChecker {

    private static final Pattern importPattern = Pattern.compile("import\\s+(\\S+);");
    private static final Pattern staticImportPattern = Pattern.compile("import\\s+static\\s+(\\S+)\\.\\S+;");

    @SneakyThrows(IOException.class)
    private static List<String> analyzeImportStatements(String file) {
        String text = FileUtils.readFileToString(Path.of(file).toFile(), Charset.defaultCharset());

        List<String> statements = new ArrayList<>();
        statements.addAll(TestUtility.extractMatches(importPattern, text, 1));
        statements.addAll(TestUtility.extractMatches(staticImportPattern, text, 1));
        return statements;
    }

    @Override
    public Dependency makeDependency(Path file) {
        String className = file.toString()
            .replace("/", ".")
            .replace("src.main.java.", "")
            .replace(".java", "");
        List<String> classNames = analyzeImportStatements(file.toString());

        return new Dependency(className, classNames);
    }

}

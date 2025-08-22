package tests.dependency.structure;

import auxiliary.TestUtil;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;

public class FileDependencyChecker extends BaseDependencyChecker {

    private static final Pattern JAVA_IMPORT_STATEMENT_PATTERN = Pattern.compile("import\\s+(\\S+);");
    private static final Pattern JAVA_STATIC_IMPORT_STATEMENT_PATTERN = Pattern.compile("import\\s+static\\s+(\\S+)\\.\\S+;");

    @Override
    public @NotNull DependencyNode makeDependencyNode(Path filePath) {
        String className = mapFilePathToClassName(filePath);
        List<String> classNames = analyzeImportStatements(filePath);

        return new DependencyNode(className, classNames);
    }

    @SneakyThrows(IOException.class)
    private static List<String> analyzeImportStatements(Path filePath) {
        String fileText = FileUtils.readFileToString(filePath.toFile(), Charset.defaultCharset());

        List<String> referenceNames = new ArrayList<>();
        referenceNames.addAll(TestUtil.collectAllMatches(JAVA_IMPORT_STATEMENT_PATTERN, fileText, 1));
        referenceNames.addAll(TestUtil.collectAllMatches(JAVA_STATIC_IMPORT_STATEMENT_PATTERN, fileText, 1));
        return referenceNames;
    }

    private static @NotNull String mapFilePathToClassName(Path file) {
        // NOTE: An example path is `/home/username/Workspace/github/fuji/fabric/src/main/java/io/github/sakurawald/fuji/Fuji.java`
        return file.toString()
            .replace("/", ".")
            .replace("src.main.java.", "")
            .replace(".java", "");
    }

}

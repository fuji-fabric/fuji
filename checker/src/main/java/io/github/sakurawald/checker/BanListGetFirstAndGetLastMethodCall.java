package io.github.sakurawald.checker;


import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.bugpatterns.BugChecker;
import java.util.List;

@AutoService(BugChecker.class)
@BugPattern(
    summary = """
        For Java version compatibility.
        """,
    severity = BugPattern.SeverityLevel.ERROR
)
public class BanListGetFirstAndGetLastMethodCall extends BanMethodCall{

    @Override
    public List<String> bannedMethodQualifiedNames() {
        return List.of(
            "java.util.List.getFirst",
            "java.util.List.getLast"
        );
    }

    @Override
    public List<String> ignoreClassQualifiedNamePrefixes() {
        return List.of();
    }

}

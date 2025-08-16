package io.github.sakurawald.checker;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.bugpatterns.BugChecker;
import java.util.List;

@AutoService(BugChecker.class)
@BugPattern(
    summary = """
        Do not call JsonObject.isEmpty()
        This method only exists in high version Gson library.
        Some target platforms may use an old version Gson, causing NoSuchMethodError thrown.
        """,
    severity = BugPattern.SeverityLevel.ERROR
)
public class BanJsonObjectIsEmptyMethodCall extends BanMethodCall {

    public BanJsonObjectIsEmptyMethodCall() {
        super(List.of("com.google.gson.JsonObject.isEmpty"), List.of());
    }
}

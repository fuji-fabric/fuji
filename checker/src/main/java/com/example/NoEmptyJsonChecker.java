package com.example;

import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.tools.javac.code.Symbol;

@BugPattern(
    name = "NoEmptyJsonChecker",
    summary = "Do not call JsonObject.isEmpty()",
    severity = BugPattern.SeverityLevel.ERROR
)
@SuppressWarnings("TreeToString")
public class NoEmptyJsonChecker extends BugChecker implements BugChecker.MethodInvocationTreeMatcher {
    @Override
    public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
        Symbol.MethodSymbol methodSymbol = ASTHelpers.getSymbol(tree);
        Symbol.ClassSymbol classSymbol = methodSymbol.enclClass();

        System.out.println("class symbol = " + classSymbol.toString());
        System.out.println("method name = " + methodSymbol.getSimpleName());

        if (classSymbol.toString().equals("com.google.gson.JsonObject") && methodSymbol.getSimpleName().toString().equals("isEmpty")) {
            return buildDescription(tree)
                .setMessage("Calling JsonObject.isEmpty() is banned")
                .build();
        }

        return Description.NO_MATCH;
    }
}

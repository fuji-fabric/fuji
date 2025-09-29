package mod.fuji.module.initializer.evaluator.evaluator.compiler.formatter;

import mod.fuji.module.initializer.evaluator.evaluator.node.LispList;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispObject;

public class LispObjectFormatter {

    public static String prettyPrint(LispObject node) {
        return prettyPrint(node, "");
    }

    private static String prettyPrint(LispObject node, String indent) {
        StringBuilder sb = new StringBuilder();

        if (node instanceof LispList listNode) {
            sb.append(indent).append("(\n");
            String childIndent = indent + "  ";
            for (LispObject childNode : listNode.getNodes()) {
                sb.append(prettyPrint(childNode, childIndent)).append("\n");
            }
            sb.append(indent).append(")");
        } else {
            sb.append(indent).append(node.toString());
        }

        return sb.toString();
    }


}

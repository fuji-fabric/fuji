package mod.fuji.module.initializer.evaluator.evaluator.compiler.formatter;

import mod.fuji.module.initializer.evaluator.evaluator.node.LispListNode;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispNode;

public class LispNodeFormatter {

    public static String prettyPrint(LispNode node) {
        return prettyPrint(node, "");
    }

    private static String prettyPrint(LispNode node, String indent) {
        StringBuilder sb = new StringBuilder();

        if (node instanceof LispListNode listNode) {
            sb.append(indent).append("(\n");
            String childIndent = indent + "  ";
            for (LispNode childNode : listNode.getNodes()) {
                sb.append(prettyPrint(childNode, childIndent)).append("\n");
            }
            sb.append(indent).append(")");
        } else {
            sb.append(indent).append(node.toString());
        }

        return sb.toString();
    }


}

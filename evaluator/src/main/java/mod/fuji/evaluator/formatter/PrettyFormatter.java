package mod.fuji.evaluator.formatter;

import java.util.List;
import mod.fuji.evaluator.auxiliary.LogUtil;

public class PrettyFormatter {

    public static void prettyPrint(List<?> list) {
        LogUtil.info("[");
        for (Object item : list) {
            LogUtil.info("  " + item + ",");
        }
        LogUtil.info("]");
    }
}

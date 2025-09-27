package mod.fuji.module.initializer.evaluator.formatter;

import java.util.List;
import mod.fuji.core.auxiliary.LogUtil;

public class PrettyFormatter {

    public static void prettyPrint(List<?> list) {
        LogUtil.info("[");
        for (Object item : list) {
            LogUtil.info("  " + item + ",");
        }
        LogUtil.info("]");
    }
}

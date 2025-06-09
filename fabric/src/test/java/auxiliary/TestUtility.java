package auxiliary;

import io.github.classgraph.ClassGraph;
import io.github.sakurawald.Fuji;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class TestUtility {

    public static ClassGraph makeBaseClassGraph() {
        return new ClassGraph()
            .acceptPackages(Fuji.class.getPackageName());
    }

    @SuppressWarnings("SameParameterValue")
    public static List<String> extractMatches(Pattern pattern, String text, int group) {
        Matcher matcher = pattern.matcher(text);

        List<String> ret = new ArrayList<>();
        while (matcher.find()) {
            ret.add(matcher.group(group));
        }

        return ret;
    }
}

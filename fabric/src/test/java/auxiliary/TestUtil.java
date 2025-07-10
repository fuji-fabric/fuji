package auxiliary;

import io.github.classgraph.ClassGraph;
import io.github.sakurawald.fuji.Fuji;
import java.nio.file.Path;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class TestUtil {

    public static final Path PROJECT_ROOT_PATH = Path.of("../");
    public static final String ROOT_PACKAGE_NAME = Fuji.class.getPackageName();

    public static ClassGraph makeBaseClassGraph() {
        return new ClassGraph()
            .acceptPackages(ROOT_PACKAGE_NAME);
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

package auxiliary;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.github.classgraph.ClassGraph;
import io.github.sakurawald.fuji.Fuji;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Path;
import lombok.Cleanup;
import lombok.SneakyThrows;
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

    @SneakyThrows(IOException.class)
    public static JsonElement readJsonElement(Path path) {
        File file = path.toFile();
        @Cleanup Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        return JsonParser.parseReader(reader);
    }
}

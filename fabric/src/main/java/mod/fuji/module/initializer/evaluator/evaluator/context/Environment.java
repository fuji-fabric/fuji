package mod.fuji.module.initializer.evaluator.evaluator.context;


import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.Value;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispSymbol;
import org.jetbrains.annotations.NotNull;

@Value
public class Environment {

    Map<String, LispSymbol> symbols = new HashMap<>();

    public static @NotNull Environment ofTopLevel() {
        return new Environment();
    }

    public Optional<LispSymbol> getSymbol(@NotNull String symbolName) {
        return Optional.ofNullable(symbols.get(symbolName));
    }


}

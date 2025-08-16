# Why `error-prone` instead of `checkstyle` and `PMD`?

1. The `error-prone` is a compiler plugin, which works inside the compiler, the richest environment full of information. It works well with `pre-processor` and `weaver` while compiling your source files. It's extensible and easy to write your own custom checkers.
2. The automatic `formatter` killed the `checkstyle` plugin. It writes its own `parser`, and can't perform `type resolution`. You can use it if you want to restrict the length of your method name.
3. The `PMD` is similar, it writes its own `parser` and its own `type resolution` module. It works better than `checkstyle`, if you don't see `generic types`, `functional interfaces` and `deeper inner classes`
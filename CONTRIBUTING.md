# Contributing

## If you are a translator...

Help us translate this project, you can do it in the following ways:
1. **The first way**: Submit your translation work via [crowdin-project](https://crowdin.com/project/fuji-fabric) using your crowdin account, and let the `crowdin-bot` create the pull-request in github for you.
2. **The second way**: Clone the `dev` branch, and do the translation work offline, then create a pull-request using `your github account` in github. (The translation files are located in [here](https://github.com/sakurawald/fuji/tree/dev/crowdin/pull-from-crowdin))

### How the language file works 
The `language file` speaks a language combines the following languages:
1. Simplified Text Language: https://placeholders.pb4.eu/user/text-format/
2. Markdown Language: https://github.com/adam-p/markdown-here/wiki/markdown-cheatsheet
3. Placeholder: the placeholder looks like `%player:name%`, and will be replaced by the contextual player. 
   1. See all available placeholders in https://placeholders.pb4.eu/user/default-placeholders/
   2. In order to do the escaping correctly, you should write `%%player:name%%` in the language file instead of `%player:name%` if the placeholder is needed.


The `language file` is formatted by the `Java standard formatter`, which follows the [specification](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Formatter.html).

Taken this example,
```java
String example1 = "the first string is %s and the second string is %s".formatted("apple", "banana");
```
The `%` character is a prefix for Java standard formatter, which indicates that the formatter will format this. 
The `s` character in string `%s` means the `type conversion`, telling the formatter to convert the `object` into a `string`.
So, the formatted result will be
```
the first string is apple and the second string is banana
```

To adjust the `argument order`, you can append `control character` to tell the formatter do so.
```java
String example2 = "the first string is %2$s and the second string is %1$s".formatted("apple", "banana");
```
The control characters `2$` in string `%2$s` tell the formatter to re-order the args it received.
So, the formatted result will be
```
the first string is banana and the second string is apple
```


## If you are a developer...

Read the chapter `Development` in fuji manual.

## Don't forget to write your name in the contributor list
You are welcome to open a `pull-request` in github to add your name into the `contributor list` locates in [here](https://github.com/sakurawald/fuji/blob/dev/fabric/src/main/resources/fabric.mod.json).
> For detailed change logs, please visit: https://github.com/sakurawald/fuji/commits/dev/
> 
> For historical change logs, refer to: https://github.com/sakurawald/fuji/releases
> 
> For user manual, refer to: https://fuji-fabric.github.io
>


# 📑 Changelog

## ✨ Added
### 🧩 Generic Predicate Commands
New commands to test **string value conditions**.

#### 🔹 Commands
- **`/equals? <player> <expectedString> <placeholderString>`**  
  Checks if a placeholder string equals the expected string.

- **`/true? <player> <placeholderString>`**  
  Returns true if the placeholder string represents a truthy value (e.g., `"true"`).

- **`/false? <player> <placeholderString>`**  
  Returns true if the placeholder string represents a falsy value (e.g., `"false"`).

- **`/matches? <player> <regex> <placeholderString>`**  
  Checks if the placeholder string matches the given regular expression.

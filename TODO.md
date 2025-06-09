# TODO

- feature: a lisp-like DSL, including a parse, transformer, analyzer, code-walker, with some built-in functions:
  predicate,
  equal...
    - similar: skript, kubejs, command macro
    - interop: /lisp eval
    - special form: setf, if, lambda, progn, quote
    - data type:
        - numeric
        - bool
        - string
        - cons
        - structure
    - domain entities:
        - entity
        - block
        - item
        - gui
    - pre-define symbols:
        - variable
        - function
            - repl
            - list operations
            - json operations
            - text operation
    - application: /air, /alert, /respawn and /spawn, /vote
- feature: command_gui module.
- feature: compare command_cooldown and command_condition.
- docs: find useful things https://vanillatweaks.net/picker/datapacks/
- Ignore the `lang/` dir in `src/` dir, we only need to copy the files from `crowdin/` for building.
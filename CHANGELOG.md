> For detailed change logs, please visit: https://github.com/sakurawald/fuji/commits/dev/
> 
> For historical change logs, refer to: https://github.com/sakurawald/fuji/releases
 
## Changelog

- [command_advice] Enhance
  - feature: introduce `accept_player_command_source` and `accept_console_command_source` fields for `matcher`, making it easier to filter the `command source`.
  - docs: add an example in `default config` to describe how to `add the exempt permission for a specified command with a specified target`.
- [predicate] feature: now `/has-perm?` and `/has-level?` commands support `offline player` as its target.
- [command_bundle] feature: new `/skull` command example in the `default config`.

The definition is: 
```markdown
    {
      "document": "This command will give the skull of specified player.",
      "requirement": {
        "level": 4,
        "string": null
      },
      "pattern": "skull <offline-player offline-player-arg>",
      "bundle": [
        "give %player:name% minecraft:player_head[minecraft:profile=$offline-player-arg]"
      ]
    }
``` 

> For detailed change logs, please visit: https://github.com/sakurawald/fuji/commits/dev/
> 
> For historical change logs, refer to: https://github.com/sakurawald/fuji/releases


## Changelog

- [warp & world] feature: increase the `auto save interval` from `10 seconds` to `1 minute`. (Makes it easier to modify the config file from the disk manually, and reload it.)
- [launcher] feature: introduced new module to provide `/launch` command.
 
```markdown
    ◉ Use a lower `angle` for `fast horizontal movement`.
    1. `/launch facing @s 15 1`
    2. `/launch facing @s 15 3.9`
    <green>The `power` is clamped in `[-3.9, +3.9]`

    ◉ Use a median `angle` for `balanced horizontal and vertical movement`.
    1. `/launch facing @s 30 3.9`
    2. `/launch facing @s 45 3.9`

    ◉ Use a higher `angle` for a `rocket launcher` effect.
    Issue: `/launch facing @s 60 3.9`
    <green>TIP: Remember to bring your `elytra`.

    ◉ Use a `vertical angle` for a `trampoline` effect.
    Issue: `/launch facing @s 90 1`

    ◉ Use a `positive power` for a `push` effect.
    Issue: `/launch facing @s 0 1`

    ◉ Use a `negative power` for a `pull` effect.
    Issue: `/launch facing @s 0 -1`

    ◉ Use another entity's perspective as the direction, to `kick` the target entity.
    Issue: `/launch at @s @e[type=!minecraft:player,distance=..8] 30 1`

    ◉ Create a `jump pad` that launches players when stepped on.
    You can integrate with `command_attachment` module.
    Issue: `/command-attachment attach-block-one ~ ~ ~ --interactType STEP_ON \\<command\\>`
```

- [command_attachment] improvements
  - feature: improve the `readbility` of `/command-attach query-{block|entity|item}` commands.
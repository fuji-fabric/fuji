> For detailed change logs, please visit: https://github.com/sakurawald/fuji/commits/dev/
> 
> For historical change logs, refer to: https://github.com/sakurawald/fuji/releases
 

## Changelog

- [command_alias] fix: support to redirect a `literal command` into another `literal command`, like redirect `/wb` to `/workbench`.
  - Before: The redirect target command must have children command nodes.
  - Now: The redirect target command can be a leaf command node.

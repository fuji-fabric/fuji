> The version number of fuji follows `semver` now: https://semver.org/


- (back module) feature: enhanced back module. (Thanks to @wolgamir)
  - feature: support `/back [lastNLocation] [targetDimension]` command, to specify the `N` for `last N location`, and `dimension` for filter.
  - feature: add `max_back_location_entries_to_savemax_back_location_entries_to_save` option, to specify max location entries to save.  
  - feature: add `fuji.back.max_location_entries_to_save` meta, to specify max location entries to save.
  - feature: add `/back list [player]` command, to list back location history.
  - feature: add `/back push` command, to push current location into location history.
  - feature: add `/back clear` command, to clear location history.
  - refactor: change the file from `config/fuji/modules/back/saved-location.json` to `config/fuji/modules/back/location-history.json`.

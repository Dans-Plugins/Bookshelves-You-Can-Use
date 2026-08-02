# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## [Unreleased]

### Fixed

- `/bycu help` and `/bycu` no longer show leftover `ExamplePonderPlugin` template text (wrong plugin name, wrong wiki link, wrong usage string) and `/bycu help`'s permission node now matches the `bycu.help` permission declared in `plugin.yml` (previously `epp.help`, which was never granted by default, silently blocking non-op players).

## [0.0.1] – 2022-08-28

### Added

- Initial release.
- Interact/click bookshelves to open a 9-slot inventory.
- Per-bookshelf inventories tracked by block location.
- Interact cooldown to prevent accidental double-opens.
- Help command (`/bycu help`).
- Debug mode configuration option.

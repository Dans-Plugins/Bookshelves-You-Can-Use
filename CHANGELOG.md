# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## [Unreleased]

### Added

- Usage reporting is now disclosed instead of quiet: the plugin prints one line on every startup saying whether reporting is on (and, if not, why), a server-wide switch `plugins/trace/config.yml` is created on first start and turns reporting off for every plugin that reports to trace when set to `enabled: false`, the environment variables `TRACE_USAGE_REPORTING=off` and `DO_NOT_TRACK=1` are honoured, and the README gains a `Usage reporting` section that says what is sent, what is not, and every way to turn it off. A `config.yml` that still lacks the `usage-reporting` block gets it written in on the next startup, with the bundled defaults, so the switch is visible where an operator would look for it. The vendored trace client is 0.2.0. Nothing about what is sent has changed. Details: https://github.com/Stephenson-Software/trace#usage-reporting
- The plugin now reports usage events — `startup` on enable, `command` on each of its commands — to the author's trace server so it is known which plugins are in use. Events carry the plugin name, the event name, and the plugin version or command name; nothing about players or the server. Reporting runs off the main thread, never delays a tick, drops silently when the server is unreachable, and is turned off with `usage-reporting.enabled: false` in `config.yml`. The default config carries the plugin's key, so reporting is active out of the box unless turned off — including on servers upgraded from a version before the `usage-reporting` block existed, whose `config.yml` is never rewritten: the plugin reads the bundled defaults for any key the file lacks. The plugin now also ships a `config.yml` in its jar, which a first start copies into the data folder before the existing `version` and `debugMode` options are added to it.
- A `Dev Release` workflow, which republishes a rolling `dev` prerelease of `main` on every non-documentation push. This is what Dan's Plugin Manager's experimental channel installs from: `/dpm get bookshelvesyoucanuse --experimental` reads `releases/tags/dev`, so without it there is nothing for that command to download. The prerelease is unreleased, unreviewed code and is marked as such.

### Fixed

- `/bycu help` now lists `/bycu` alongside `/bycu help`. `COMMANDS.md` describes the help command as displaying the list of available commands and documents both, but only the second was printed, so the plugin-information command was invisible to a player who never read the documentation.
- The `Dev Release` workflow now retries publishing the `dev` prerelease before giving up. The release and its tag have to be deleted and recreated for the tag to move to the new commit, and a transient API failure inside that window previously left the repository with no `dev` release at all until the workflow was re-run by hand. Each attempt now starts from a clean slate, and an exhausted retry fails loudly.
- Left-clicking a bookshelf no longer opens its inventory. Only right-clicks do, so bookshelves can be broken normally again — previously the inventory re-opened on every punch, which interrupted block breaking.
- A right-click that opens a bookshelf is now cancelled, so a held block is no longer placed against the bookshelf while its inventory opens.
- The plugin's config file is now located through Bukkit's data folder rather than a hardcoded `./plugins/<name>/config.yml` path. On a server whose working directory or plugins directory differs from the assumed layout, the file was reported as missing and the config defaults were rewritten on every startup instead of the compatibility checks running.
- `.gitignore` now ignores `*.iml` rather than the `ExamplePonderPlugin.iml` template leftover, so an IntelliJ module file generated for this project is no longer committable by accident.

## [0.1.0-SNAPSHOT-8-8-2026] – 2026-08-08

### Changed
- Bookshelves-You-Can-Use is now developed AI-first. Day-to-day feature work, grooming, review and maintenance run through AI agents working directly against this repository, with the maintainers setting direction and approving what lands. The version bump marks that change in how the project is built — it is not a break in behaviour, configuration or stored data, and existing installations can upgrade in place. Released as `0.1.0-SNAPSHOT-8-8-2026`: the AI-first line has not yet been verified in live operation, and the dated snapshot designation stays until it has.

### Removed

- Unreachable config-editing code in `ConfigService` (`setConfigOption`, `sendConfigList`, and the `hasBeenAltered` flag that only `setConfigOption` ever wrote), including leftover `ExamplePonderPlugin` template branches for placeholder config options named `A` and `C` that this plugin never had. No command exposed any of it, so plugin behavior is unchanged.

### Fixed

- `/bycu help` and `/bycu` no longer show leftover `ExamplePonderPlugin` template text (wrong plugin name, wrong wiki link, wrong usage string) and `/bycu help`'s permission node now matches the `bycu.help` permission declared in `plugin.yml` (previously `epp.help`, which was never granted by default, silently blocking non-op players).
- `CONTRIBUTING.md` no longer instructs contributors to update a `src/main/resources/lang/` directory that doesn't exist; this plugin has no localization/lang file system.

## [0.0.1] – 2022-08-28

### Added

- Initial release.
- Interact/click bookshelves to open a 9-slot inventory.
- Per-bookshelf inventories tracked by block location.
- Interact cooldown to prevent accidental double-opens.
- Help command (`/bycu help`).
- Debug mode configuration option.

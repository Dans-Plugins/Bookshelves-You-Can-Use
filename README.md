# Bookshelves You Can Use

## Description

Bookshelves You Can Use is a Minecraft plugin that turns bookshelf blocks into usable storage containers. Right-click any bookshelf block to open a 9-slot inventory where you can store and retrieve items.

## Installation

### First Time Installation

1. Download the plugin JAR.
2. Place the JAR in the `plugins` folder of your Spigot server.
3. Restart your server.

## Usage

### Documentation

- [User Guide](USER_GUIDE.md) – Getting started and common scenarios
- [Commands Reference](COMMANDS.md) – Complete list of all commands
- [Configuration Guide](CONFIG.md) – Detailed configuration options

## Support

You can find the support Discord server [here](https://discord.gg/xXtuAQ2).

### Experiencing a bug?

Please fill out a bug report [here](https://github.com/Dans-Plugins/Bookshelves-You-Can-Use/issues/new).

- [Known Bugs](https://github.com/Dans-Plugins/Bookshelves-You-Can-Use/issues?q=is%3Aissue+is%3Aopen+label%3Abug)

## Usage reporting

Usage reporting is on by default: when the plugin is enabled, and each time one of its commands is used, it sends its name, its version and the command's name to the author's trace server at `https://trace.danielstephenson.dev`, so it is known which plugins are actually in use. Nothing about players, worlds, IP addresses or the server is sent, and nothing typed after a command. The plugin prints one line on every startup saying whether reporting is on and, if not, why.

To turn it off:

- for this plugin only: `usage-reporting.enabled: false` in `plugins/BookshelvesYouCanUse/config.yml`
- for every plugin on the server that reports to trace: `enabled: false` in `plugins/trace/config.yml` (created on first start; plugins never turn it back on)
- for the whole server process: the environment variable `TRACE_USAGE_REPORTING=off` or `DO_NOT_TRACK=1`

Details: https://github.com/Stephenson-Software/trace#usage-reporting

## Contributing

- [CONTRIBUTING.md](CONTRIBUTING.md)

## Testing

### Build

Linux / macOS:

    mvn clean package

Windows:

    mvn clean package

If you see `BUILD SUCCESS`, the build has passed.

## Development

### Local Testing

1. Build the plugin: `mvn clean package`
2. Copy the JAR from `target/` into your test server's `plugins/` folder.
3. Start the server.

## Authors and Acknowledgement

### Developers

| Name | Main Contributions |
|------|--------------------|
| Daniel McCoy Stephenson | Creator and primary developer |

## License

This project is licensed under the [GNU General Public License v3.0](LICENSE) (GPL-3.0).

You are free to use, modify, and distribute this software, provided that:

- Source code is made available under the same license when distributed.
- Changes are documented and attributed.
- No additional restrictions are applied.

See the [LICENSE](LICENSE) file for the full text of the GPL-3.0 license.

## Project Status

This project is in maintenance mode.

### Changelog

See [CHANGELOG.md](CHANGELOG.md) for a release-by-release summary of changes.

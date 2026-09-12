# Configuration Guide

This document explains every option in the plugin's `config.yml`.

## version

**Type:** string
**Default:** Current plugin version (e.g. `v0.1.0-SNAPSHOT-8-8-2026`)
**Description:** The version of the plugin that last saved the configuration. This is managed automatically and should not be changed manually.

```yaml
version: v0.1.0-SNAPSHOT-8-8-2026
```

## debugMode

**Type:** boolean
**Default:** `false`
**Description:** Enables or disables debug mode. When enabled, additional debug information may be logged to the server console.

```yaml
debugMode: false
```

## usage-reporting

**Type:** section
**Default:** see below
**Description:** When the plugin is enabled, and each time one of its commands is used, a small event is sent to the author's [trace](https://github.com/Stephenson-Software/trace-client-java) server so it is known which plugins are actually in use. An event carries the plugin's name, the event name (`startup` or `command`), and either the plugin version or the command name — nothing about players, the world, or the server. Sending happens off the main thread, never delays a tick, and is dropped silently if the server cannot be reached. Set `usage-reporting.enabled` to `false` to turn it off.

A `config.yml` written by a version before this block existed is not rewritten with it; the plugin reads the bundled defaults for any key the file lacks, so reporting is active on upgraded servers too unless turned off.

| Key | Default | Description |
|-----|---------|-------------|
| `usage-reporting.enabled` | `true` | Whether the plugin reports usage events. Set to `false` to turn it off. |
| `usage-reporting.endpoint` | `https://trace.danielstephenson.dev` | The trace server events are sent to. |
| `usage-reporting.key` | the plugin's key | Identifies this plugin to the trace server so reports are attributed to it. Not a secret: it ships in the default config and can only report as BookshelvesYouCanUse. Empty means reporting is off regardless of `enabled`. |

```yaml
usage-reporting:
  enabled: true
  endpoint: https://trace.danielstephenson.dev
  key: "HLRDrzDEke1MlSFEHSrcSM1_kvUxwJqlMTiE4-1LeNA"
```

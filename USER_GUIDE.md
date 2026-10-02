# User Guide

## Prerequisites

- A Minecraft server running Spigot (or a Spigot-compatible fork) on one of the versions listed in [`minecraft-versions.json`](minecraft-versions.json): currently 1.19.4, 1.21.11 and 26.2. Other versions from 1.19.4 onwards are expected to work but are not tested (see the README's [Supported Minecraft Versions](README.md#supported-minecraft-versions)).
- The Java runtime that your Minecraft version requires. The plugin is compiled for Java 9 bytecode, so it adds no Java requirement of its own.
- The [Ponder](https://github.com/Preponderous-Software/Ponder) library (already bundled inside the plugin JAR; no separate installation required).

## First Steps

1. Install the plugin by placing the JAR file in your server's `plugins/` folder.
2. Restart the server.
3. Right-click any bookshelf block in the world to open its inventory.

## Common Scenarios

### Storing Items in a Bookshelf

1. Walk up to a bookshelf block in the world.
2. Right-click the bookshelf.
3. A 9-slot inventory will open with the title "Bookshelf".
4. Place items into the inventory.
5. Close the inventory. Your items will remain in the bookshelf until the server restarts.

### Retrieving Items from a Bookshelf

1. Right-click a bookshelf that already has items stored in it.
2. Take the items you need from the inventory.
3. Close the inventory.

### Interact Cooldown

After opening a bookshelf, there is a 2-second cooldown before you can open a bookshelf again — the same one or any other. This prevents accidental double-opens. A right-click during the cooldown does not open anything and is not cancelled, so it behaves as it would without the plugin (a held block can be placed against the bookshelf).

## Permissions

| Permission | Default | Description |
|------------|---------|-------------|
| `bycu.help` | `true` | Allows the player to use the help command. |

## Notes

- Bookshelf inventories are stored in memory and are **not** persisted across server restarts.
- Each bookshelf block in the world has its own independent inventory.
- An inventory belongs to the block position, not the block. Breaking a bookshelf does **not** drop the items stored in it; they stay at that position, and a bookshelf placed there later opens with them.
- Only right-clicks open a bookshelf. Left-clicking is left alone, so bookshelves are broken the usual way.
- A right-click that opens a bookshelf is cancelled, so a held block is not placed against the bookshelf at the same time.

# User Guide

## Prerequisites

- A Minecraft server running Spigot (or a Spigot-compatible fork) version 1.13 or later.
- The [Ponder](https://github.com/Preponderous-Software/Ponder) library must be available (it is shaded into the plugin JAR automatically).

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

After interacting with a bookshelf, there is a 2-second cooldown before you can interact with another bookshelf. This prevents accidental double-opens.

## Permissions

| Permission | Default | Description |
|------------|---------|-------------|
| `bycu.help` | `true` | Allows the player to use the help command. |

## Notes

- Bookshelf inventories are stored in memory and are **not** persisted across server restarts.
- Each bookshelf block in the world has its own independent inventory.

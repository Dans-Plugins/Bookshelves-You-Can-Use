# Commands Reference

All commands can be accessed via `/bookshelvesyoucanuse` or the shorter alias `/bycu`.

## General Commands

### /bycu

**Description:** Displays plugin information including the version, developer, and wiki link.
**Permission:** None
**Usage:** `/bycu`

### /bycu help

**Description:** Displays the list of available commands.
**Permission:** `bycu.help`
**Usage:** `/bycu help`

Anything typed after `help` is ignored. A player whose permissions deny `bycu.help` is refused with a message naming the permission.

## Unknown Subcommands

Any other subcommand is answered with "That command wasn't found." Subcommand names are matched exactly, so `/bycu HELP` is unknown too.

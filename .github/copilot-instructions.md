# Copilot Instructions

This repository follows the DPC (Dans Plugins Community) conventions defined at
https://github.com/Dans-Plugins/dpc-conventions. Read those conventions before
making any changes.

## Technology Stack

- Language: Java
- Build tool: Maven
- Target platform: Spigot (Minecraft plugin)
- Framework: [Ponder](https://github.com/Preponderous-Software/Ponder)

## Project Structure

- `src/main/java/` – Plugin source code
  - `dmccoystephenson/bookshelvesyoucanuse/` – Main package
  - `commands/` – Command executors
  - `data/` – Runtime data holders
  - `eventhandlers/` – Bukkit event listeners
  - `exceptions/` – Custom exception classes
  - `objects/` – Domain objects
  - `services/` – Service classes (e.g. config)
- `src/main/resources/` – `plugin.yml` and resource files

## Coding Conventions

- Follow the existing package structure when adding new classes.
- Use the Ponder framework's `AbstractPluginCommand` for new commands.
- Annotate every command executor and event listener with `@Override` where applicable.

## Contribution Workflow

- Branch from `main` for all changes.
- Open a pull request against `main`.
- Reference the related GitHub issue in every pull request description.

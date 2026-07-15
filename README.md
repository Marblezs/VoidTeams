# VoidTeams

VoidTeams is a lightweight team management plugin for Minecraft 1.21.x. It’s designed to be fast, simple, and highly customizable for UHC or competitive setups.

## Features
* **Flexible Team Modes:** Choose between "Choosen" (invite-based) or "Random" (automated) teams.
* **Team Chat:** Toggleable chat channels to keep your team communication private.
* **Customization:** Configurable icons, colors, and team sizes via `config.yml`.
* **PlaceholderAPI Support:** Integrated hooks for your scoreboard or chat plugins.
* **Admin Tools:** Full control to force joins, shuffle teams, or lock the entire system.

---

## Commands

### User Commands (`/team` or `/t`)
| Command | Description |
| :--- | :--- |
| `/team invite <player>` | Invite a player to your team. |
| `/team accept <player>` | Accept an incoming team invitation. |
| `/team leave` | Leave your current team. |
| `/team color` | Change your team's display color. |
| `/team chat` | Toggle your private team chat. |

### Admin Commands (`/teamadmin` or `/ta`)
| Command | Description |
| :--- | :--- |
| `/ta force <p1> <p2>` | Force p1 into p2's team. |
| `/ta type <Random/Choosen> <size>` | Set the team mode and max size. |
| `/ta remove <player>` | Remove a player from their team. |
| `/ta disband <player>` | Disband a player's team. |
| `/ta clear` | Remove all existing teams. |
| `/ta color/icon <player>` | Change a specific team's color or icon. |
| `/ta shuffle` | Randomize all online players into teams. |
| `/ta shuffleforce` | Randomize only players currently without a team. |
| `/ta block <all/chat/teams/none>` | Lock specific system functions. |
| `/ta reload` | Reload the plugin configuration. |

---

## Placeholders
Requires [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) installed.

* `%voidteams_size%` - Returns the current team size.
* `%voidteams_type%` - Returns the current team mode (Random/Choosen).
* `%voidteams_team%` - Returns the player's team name/ID.

---

## Installation
1. Drop `VoidTeams.jar` into your `/plugins` folder.
2. Restart your server.
3. Edit the `config.yml` to your liking and run `/teamadmin reload`.
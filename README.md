# VoidTeams

VoidTeams is a lightweight team management plugin for Minecraft 1.21.x. It’s designed to be fast, simple, and highly customizable for UHC or competitive setups.

## Features
* **Flexible Team Modes:** Choose between "Choosen" (invite-based), "Random" (automated), or "Vote" modes.
* **Interactive Chat & Invites:** Clickable invitations in chat and toggleable private team communication channels.
* **Live Voting System:** Real-time action bar voting timers showing live results for configuration changes.
* **Customization:** Configurable icons, colors, and team sizes via `config.yml`.
* **PlaceholderAPI Support:** Integrated hooks for your scoreboard or chat plugins, including dynamic member lists and distances.
* **Admin Tools:** Full control to force joins, shuffle teams, lock systems, and reload configurations on the fly.

---

## Commands

### User Commands (`/team` or `/t`)
| Command | Description |
| :--- | :--- |
| `/team invite <player>` | Invite a player to your team (with clickable accept feature). |
| `/team accept <player>` | Accept an incoming team invitation. |
| `/team leave` | Leave your current team. |
| `/team color` | Change your team's display color. |
| `/team chat` | Toggle your private team chat. |

### Admin Commands (`/teamadmin` or `/ta`)
| Command | Description |
| :--- | :--- |
| `/ta force <p1> <p2>` | Force player 1 into player 2's team. |
| `/ta type <Choosen/Random/Vote>` | Set the team mode. |
| `/ta size <set/add/remove> <val>` | Modify maximum team capacity. |
| `/ta remove <player>` | Remove a player from their team. |
| `/ta disband <player>` | Disband a player's entire team. |
| `/ta clear` | Remove all existing teams. |
| `/ta color <player> <color>` | Change a specific team's color. |
| `/ta icon <player> <icon>` | Change a specific team's icon. |
| `/ta shuffle` | Randomize online players into teams (respects current mode). |
| `/ta shuffleforce` | Force randomize only players currently without a team. |
| `/ta vote <type|size> <options...>` | Start an interactive live vote with action bar timers. |
| `/ta vote stop` | Stop the active vote immediately. |
| `/ta block <all/chat/teams/none>` | Lock or unlock specific system functions. |
| `/ta reload` | Reload the plugin configuration. |

---

## Placeholders
Requires [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) installed.

| Placeholder | Description |
| :--- | :--- |
| `%voidteams_team%` | Returns the prefix/color of the player's team. |
| `%voidteams_type%` | Returns the current team mode (Random/Choosen/Vote). |
| `%voidteams_size%` | Returns the numeric value of the max team size. |
| `%voidteams_teamsize%` | Returns "FFA" if size is 1, or format like `<type> to <size>`. |
| `%voidteams_has_team%` | Returns "true" or "false" depending on whether the player is in a team. |
| `%voidteams_member_<index>%` | Returns formatted member details (Head icon, online/offline color status, name, and live distance tracking, e.g., `%voidteams_member_1%`). |

---

## Installation & Download
* **Download JAR:** [MediaFire Link](https://www.mediafire.com/file/krrte22jmkmkl26/VoidTeams-1.0.jar/file)

1. Drop `VoidTeams.jar` into your `/plugins` folder.
2. Restart your server.
3. Edit the `config.yml` to your liking and run `/teamadmin reload`.
# VoidTeams 2.2.0

## Team Scenario framework

- Nuevo `TeamScenarioManager`.
- Nuevo `TeamScenario` base para listeners y módulos independientes.
- Nuevo `team-scenarios.yml`.
- Estados persistentes por scenario.
- Conflictos automáticos entre `Captains` y `Auction`.
- `/teamadm scen list|toggle|on|off|info|reload`.
- Soporte también para `/teamadm scen <scenario> ...`.
- Placeholder `%voidteams_scenarios%` ahora se construye dinámicamente.
- Placeholders dinámicos `%voidteams_scenario_<id>%` y `%voidteams_scenario_<id>_display%`.

## Team Inventory

- Convertido formalmente en Team Scenario.
- `/ti` conserva inventario compartido y combat-lock silencioso.
- Settings migrados a `team-scenarios.yml`.
- `/teamadm teaminventory ...` se conserva como alias retrocompatible.

## Shared Health

- Nuevo scenario funcional.
- Replica la pérdida real de vida al resto del equipo.
- Opción de compartir curación.
- Lista configurable de mundos ignorados.
- Feedback por ActionBar y SoundManager.

## Captains

- Nuevo snake draft funcional.
- Selección automática de capitanes según jugadores y TeamSize.
- `/team pick <jugador>`.
- Timer por turno con auto-pick.
- Bloqueo temporal de modificaciones normales de equipos durante el draft.
- Placeholders de turno, tiempo y jugadores restantes.

## Auction

- Nuevo sistema de subasta funcional.
- Capitanes, créditos iniciales, pool aleatorio y pujas.
- `/team bid <cantidad>`.
- Incremento mínimo y anti-snipe configurables.
- Asignación balanceada cuando nadie puja.
- Placeholders de jugador, bidder, bid, tiempo y créditos.

## Team Config

- Nuevo `/teamadm config` para ver y modificar configuración base.
- `teams-locked` y `team-chat-locked` persistentes en `config.yml`.
- Nuevo `%voidteams_configs%` para GUI/Discord.

## Utilidades

- `/tl` y `/mores` permanecen como utilidades; no aparecen en `%voidteams_scenarios%`.

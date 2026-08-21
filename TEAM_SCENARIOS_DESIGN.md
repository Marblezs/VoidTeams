# VoidTeams Team Scenarios — arquitectura 2.2

El sistema ya está implementado mediante `TeamScenarioManager` y clases que extienden `TeamScenario`.

## Qué es un Team Scenario

Un Team Scenario modifica cómo cooperan o se forman los equipos. No debe absorber utilidades normales.

Sí son scenarios:
- Team Inventory
- Shared Health
- Captains
- Auction

No son scenarios:
- `/tl`
- `/mores`
- Team Chat
- TeamSize
- Friendly Fire

## Ciclo

Cada scenario tiene:
- `id`
- `displayName`
- `description`
- `icon`
- estado `active`
- listeners propios
- `onEnableScenario()` / `onDisableScenario()`
- comandos administrativos opcionales
- settings en `team-scenarios.yml`

Los listeners se registran una vez. Cada listener verifica `isActive()`.

## Integración con VoidUHC

No se importa `VoidTeams` dentro de VoidUHC para mostrar reglas. La integración visual se hace con PlaceholderAPI:

```text
%voidteams_scenarios%
%voidteams_configs%
%voidteams_scenario_team_inventory_display%
%voidteams_scenario_shared_health_display%
%voidteams_scenario_captains_display%
%voidteams_scenario_auction_display%
```

Así BetterGUI/GUI de scenarios y el post de Discord pueden consumir el estado sin acoplar ambos plugins.

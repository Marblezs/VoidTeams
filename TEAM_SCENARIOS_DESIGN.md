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

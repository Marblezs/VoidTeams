# VoidTeams Public API

VoidTeams registra `VoidTeamsAPI` en Bukkit `ServicesManager` y también publica `VoidTeamsProvider`.

## Carga recomendada

```
VoidTeamsAPI api = Bukkit.getServicesManager().load(VoidTeamsAPI.class);
if (api == null) return;
```

En el plugin consumidor usa `softdepend: [VoidTeams]`.

## Scenarios para VoidUHC

```
for (TeamScenarioInfo scenario : api.getScenarios()) {
    String id = scenario.id();
    String name = scenario.displayName();
    String description = scenario.description();
    Material icon = scenario.icon();
    boolean enabled = scenario.enabled();
}
```

Para un click del ScenarioGUI:

```
api.toggleScenario(id);
```

Los items del GUI pueden guardar:

```
source = voidteams
scenario_id = shared_health
```

De esta forma VoidUHC puede mezclar sus scenarios con los de VoidTeams sin importar managers internos.

## Eventos

```
@EventHandler
public void onTeamScenarioChange(TeamScenarioStateChangeEvent event) {
    String id = event.getScenarioId();
    boolean enabled = event.isEnabled();
}
```

## Equipos

La API expone `getTeam(Player)`, `getTeam(String)`, `getTeams()`, TeamSize, tipo, Friendly Fire, locks, colores e iconos.

## Provider

```
VoidTeamsAPI api = VoidTeamsProvider.get();
```

Las operaciones `setScenarioEnabled` y `toggleScenario` deben ejecutarse en el hilo principal de Bukkit.

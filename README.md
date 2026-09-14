# VoidTeams 2.2

VoidTeams es el plugin de equipos que acompaña a VoidUHC Closed code. Su responsabilidad sigue siendo únicamente el sistema de teams: creación, invitaciones, Team Chat, RGB, TeamSize, votaciones, utilidades de equipo y ahora **Team Scenarios** independientes de los scenarios normales de VoidUHC.

## Team Scenarios

Los Team Scenarios viven en `team-scenarios.yml` y son administrados por `TeamScenarioManager`.

Incluidos en 2.2:

- **Team Inventory** — `/ti`, mochila compartida por equipo y bloqueo silencioso durante combat-tag.
- **Shared Health** — el daño real recibido por un compañero se replica al resto del equipo; opcionalmente también la curación.
- **Captains** — selección aleatoria de capitanes + snake draft mediante `/team pick <jugador>`.
- **Auction** — capitanes con créditos que pujan por jugadores mediante `/team bid <cantidad>`.

`/tl` y `/mores` continúan siendo **utilidades**, no scenarios.

### Administración

```text
/teamadm scen list
/teamadm scen toggle <scenario>
/teamadm scen on <scenario>
/teamadm scen off <scenario>
/teamadm scen info <scenario>
/teamadm scen reload
```

También funciona el formato scenario-first:

```text
/teamadm scen team_inventory toggle
/teamadm scen shared_health on
/teamadm scen captains start
/teamadm scen auction start
```

Aliases aceptados para escenarios:

```text
team_inventory / teaminventory / ti
shared_health / sharedhealth
captains / captain
auction
```

## Team Inventory

```text
/ti
/teamadm scen team_inventory size 27
/teamadm scen team_inventory combatlock 5
/teamadm scen team_inventory see <jugador>
```

El alias antiguo sigue disponible:

```text
/teamadm teaminventory toggle|on|off|see
```

El contenido se guarda en `team-inventories.yml`. Si el jugador causa o recibe daño PvP, `/ti` queda bloqueado silenciosamente durante los segundos configurados.

## Shared Health

Cuando está activo, VoidTeams calcula la vida realmente perdida después del evento de daño y replica esa pérdida al resto de compañeros online. La curación compartida puede activarse/desactivarse.

```text
/teamadm scen shared_health healing on
/teamadm scen shared_health healing off
/teamadm scen shared_health ignoreworld list
/teamadm scen shared_health ignoreworld add lobby
/teamadm scen shared_health ignoreworld remove lobby
```

Por defecto `lobby` y `arena` están excluidos en `team-scenarios.yml`.

## Captains

1. Define el TeamSize.
2. Activa Captains.
3. Inicia el draft.
4. VoidTeams selecciona los capitanes y crea sus equipos.
5. Los turnos avanzan en snake draft.
6. Cada capitán usa `/team pick <jugador>`.
7. Si se agota el tiempo, se realiza un pick automático.

```text
/teamadm scen captains on
/teamadm scen captains start
/teamadm scen captains status
/teamadm scen captains picktime 30
/teamadm scen captains stop
```

Durante el draft se bloquean temporalmente las modificaciones normales de equipos y se restaura el bloqueo anterior al terminar.

## Auction

1. Define TeamSize.
2. Activa Auction.
3. Inicia la subasta.
4. VoidTeams selecciona capitanes y entrega créditos.
5. Va presentando jugadores del pool.
6. Los capitanes usan `/team bid <créditos>`.
7. La puja más alta incorpora al jugador al equipo ganador.
8. Si nadie puja, el jugador se asigna gratuitamente al equipo con más necesidad de miembros.

```text
/teamadm scen auction on
/teamadm scen auction start
/teamadm scen auction status
/teamadm scen auction credits 100
/teamadm scen auction bidtime 15
/teamadm scen auction increment 5
/teamadm scen auction stop
```

## Configuración rápida del host

La configuración base no se mezcla con los Team Scenarios.

```text
/teamadm config
/teamadm config friendlyfire on|off
/teamadm config chat on|off
/teamadm config teams on|off
/teamadm config size <n>
/teamadm config type Choosen|Random|Vote
```

Los bloqueos `teams-locked` y `team-chat-locked` se guardan en `config.yml`.

## Utilidades de equipo

### `/tl`

Envía X/Y/Z y dimensión exclusivamente al chat del equipo.

### `/mores`

Envía los ores minados del jugador a su equipo. Los ores colocados por jugadores no vuelven a contar al romperlos en la misma sesión.

## Placeholders para VoidUHC / GUI / Discord

Los placeholders globales funcionan sin jugador, por lo que VoidUHC puede resolverlos dentro del post de Discord o de una GUI.

### Team Scenarios

```text
%voidteams_scenarios%
%voidteams_scenarios_compact%
%voidteams_scenarios_count%

%voidteams_scenario_team_inventory%
%voidteams_scenario_team_inventory_display%
%voidteams_scenario_shared_health%
%voidteams_scenario_shared_health_display%
%voidteams_scenario_captains%
%voidteams_scenario_captains_display%
%voidteams_scenario_auction%
%voidteams_scenario_auction_display%

# También disponible para cualquier ID de scenario:
%voidteams_scenario_<id>_state%
%voidteams_scenario_<id>_description%
%voidteams_scenario_<id>_icon%
```

Ejemplo:

```text
%voidteams_scenarios%
-> Team Inventory, Shared Health
```

Esto permite añadir en la GUI de scenarios de VoidUHC una línea como:

```text
Team Scenarios: %voidteams_scenarios%
```

O construir botones separados, por ejemplo para Shared Health:

```text
Nombre: %voidteams_scenario_shared_health_display%
Estado: %voidteams_scenario_shared_health_state%
Descripción: %voidteams_scenario_shared_health_description%
Icono Bukkit: %voidteams_scenario_shared_health_icon%
```

VoidUHC solo necesita pasar el texto por PlaceholderAPI; no necesita depender de las clases internas de VoidTeams.

sin que VoidUHC importe clases internas de VoidTeams.

### Team Config

```text
%voidteams_configs%
%voidteams_config_friendlyfire%
%voidteams_config_chat%
%voidteams_config_teams%
%voidteams_teamsize%
%voidteams_mode%
%voidteams_type_raw%
```

Ejemplo:

```text
%voidteams_configs%
-> Cto2 • FF Off • Chat On • Teams Open
```

### Captains runtime

```text
%voidteams_captains_active%
%voidteams_captains_turn%
%voidteams_captains_time%
%voidteams_captains_remaining%
```

### Auction runtime

```text
%voidteams_auction_active%
%voidteams_auction_player%
%voidteams_auction_bidder%
%voidteams_auction_bid%
%voidteams_auction_time%
%voidteams_auction_credits%
```

### Otros placeholders existentes

```text
%voidteams_team%
%voidteams_team_color%
%voidteams_team_icon%
%voidteams_team_count%
%voidteams_type%
%voidteams_type_raw%
%voidteams_size%
%voidteams_teamsize%
%voidteams_mode%
%voidteams_has_team%
%voidteams_chat%
%voidteams_teaminventory%
%voidteams_teaminventory_state%
%voidteams_teaminventory_display%
%voidteams_teaminventory_combat%
%voidteams_mores_total%
%voidteams_vote_active%
%voidteams_vote_time%
%voidteams_vote_leader%
%voidteams_member_1%
%voidteams_member_2%
```

## Archivos

```text
config.yml
team-scenarios.yml
team-inventories.yml       # generado en runtime
team-ores.yml              # generado en runtime
```

## Arquitectura

```text
me.VoidTeams
├── managers
│   ├── TeamManager
│   ├── TeamScenarioManager
│   ├── TeamInventoryManager
│   ├── OreTrackerManager
│   └── ...
│
└── scenarios
    ├── TeamScenario
    ├── TeamInventoryScenario
    ├── SharedHealthScenario
    ├── CaptainsScenario
    └── AuctionScenario
```

Los listeners de cada scenario se registran una sola vez. El estado activo se controla desde `TeamScenarioManager`, por lo que los scenarios pueden activarse/desactivarse sin volver a registrar listeners.

## API pública

VoidTeams 2.2 expone una API estable para plugins externos. No es necesario acceder a `TeamManager` o `TeamScenarioManager` directamente.

Con `ServicesManager`:

```java
VoidTeamsAPI api = Bukkit.getServicesManager().load(VoidTeamsAPI.class);
if (api == null) return;

for (TeamScenarioInfo scenario : api.getScenarios()) {
    String id = scenario.id();
    String name = scenario.displayName();
    Material icon = scenario.icon();
    boolean enabled = scenario.enabled();
}

api.toggleScenario("shared_health");
```

También está disponible el provider:

```java
VoidTeamsAPI api = VoidTeamsProvider.get();
```

La API pública también expone TeamSize, tipo de equipos, Friendly Fire, locks, teams, miembros, colores, iconos y la paleta RGB disponible.

Cambios de estado de scenarios pueden escucharse mediante `TeamScenarioStateChangeEvent`.

Las operaciones que modifican estado deben ejecutarse desde el hilo principal de Bukkit.

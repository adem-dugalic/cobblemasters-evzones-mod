# CobbleMasters EV Zones

A Fabric mod for **Cobblemon 1.7.3 / Minecraft 1.21.1** that manages configurable EV training zones. Each zone automatically spawns a specific Pokémon at a fixed rate until a cap is reached — no command blocks, no scoreboards.

## How It Works

- You define a zone by standing inside your fenced area and running `/evzone create`
- The mod spawns the specified Pokémon at a random position within the zone radius
- It counts how many Pokémon are already in the zone — if the cap is hit, it stops spawning
- When players kill Pokémon, the count drops and spawning resumes automatically
- All zones persist through server restarts via `config/cobblemasters-evzones.json`

---

## Installation

1. Drop `cobblemasters-evzones-mod-1.0.0.jar` into your server's `mods/` folder
2. Requires **Fabric API** and **Cobblemon** to be installed
3. Start the server — a config file is created at `config/cobblemasters-evzones.json`

---

## Commands

All commands require **OP level 2**.

### Create a zone
Stand inside your fenced area, then run:
```
/evzone create <name> <radius> <maxcount> <rateticks> <pokemon>
```

| Argument | Description |
|----------|-------------|
| `name` | Unique zone name (no spaces) |
| `radius` | Spawn radius in blocks |
| `maxcount` | Max Pokémon allowed in the zone at once |
| `rateticks` | Ticks between each spawn attempt (20 ticks = 1 second) |
| `pokemon` | Pokémon to spawn — supports Cobblemon properties |

**Example:**
```
/evzone create attack_zone 10 8 200 machop level:10
/evzone create speed_zone 10 8 200 diglett level:10
/evzone create spatk_zone 10 8 200 gastly level:10
```

**Rate reference:**

| Ticks | Time |
|-------|------|
| 100 | 5 seconds |
| 200 | 10 seconds |
| 400 | 20 seconds |
| 600 | 30 seconds |

---

### Manage zones

```
/evzone list                        — List all active zones
/evzone info <name>                 — Show details for a zone
/evzone delete <name>               — Delete a zone
/evzone tp <name>                   — Teleport to a zone center
/evzone reload                      — Reload zones from config file
```

### Modify a zone

```
/evzone setpokemon <name> <pokemon> — Change the Pokémon (supports properties)
/evzone setmax <name> <count>       — Change the max Pokémon cap
/evzone setrate <name> <ticks>      — Change the spawn rate
```

**Examples:**
```
/evzone setpokemon attack_zone machoke level:20
/evzone setmax speed_zone 5
/evzone setrate attack_zone 100
```

---

## Pokémon Properties

The `pokemon` argument supports any Cobblemon property string:

```
machop
machop level:15
gastly level:10 shiny:true
diglett level:5
```

---

## EV Zone Reference

| EV Stat | Recommended Pokémon |
|---------|-------------------|
| Attack | `machop` |
| Defense | `geodude` |
| Sp. Attack | `gastly` |
| Sp. Defense | `tentacool` |
| Speed | `diglett` |
| HP | `chansey` |

---

## Config File

Zones are stored at `config/cobblemasters-evzones.json`. You can edit this directly and run `/evzone reload` to apply changes without restarting the server.

```json
{
  "zones": [
    {
      "name": "attack_zone",
      "dimension": "minecraft:overworld",
      "x": 100.0,
      "y": 64.0,
      "z": 200.0,
      "radius": 10.0,
      "pokemon": "machop level:10",
      "maxCount": 8,
      "spawnRateTicks": 200
    }
  ]
}
```

# DoomOrbPlugin

A RuneLite helper overlay for the moving safety boulder / orb-flower phase.

## What this project includes

- Orb tracking
- Best target scoring
- First + second orb pair selection
- Straight-line / diagonal preference
- Predicted run-line overlay between the chosen pair
- Simple "safe side" hint from pair geometry
- Tick-age labels for spawned orbs
- Info panel showing phase state and timing
- Config options for colors and overlays

## Important note

This plugin is **visual only**:
- no clicks
- no input automation
- no target switching
- no interaction with game controls

## Build

```bash
gradlew build
```

or on Linux/macOS if you add your own wrapper:

```bash
./gradlew build
```

## Output

The built jar will be in:

```bash
build/libs/DoomOrbPlugin-1.1.jar
```

## How the smarter selection works

Each orb gets a score based on:
- distance from player
- row/column/diagonal alignment with the player
- age of the orb spawn
- centrality penalty for awkward pathing

Then pairs are scored using:
- distance between first and second orb
- straight or diagonal connection
- closeness of the pair's path to the player
- preference for a route that does not fold back on itself

## Files

- `DoomOrbPlugin.java`
- `DoomOrbConfig.java`
- `DoomOrbSceneOverlay.java`
- `DoomOrbInfoOverlay.java`
- `DoomOrbTracker.java`
- `OrbScorer.java`
- `OrbPair.java`
- `TrackedOrb.java`
- `PhaseState.java`

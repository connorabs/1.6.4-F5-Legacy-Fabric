# Omnilook Lite (1.6.4 / Legacy Fabric)

A minimal, single-version freelook mod inspired by [Omnilook](https://github.com/rhysdh540/Omnilook),
built for Minecraft 1.6.4 on Legacy Fabric. This is **not** the real Omnilook mod/source -
## What it does

Two independent, keybindable modes (only one active at a time):

| Mode | Default key | Behaviour |
|---|---|---|
| **Camera Freelook** | `` ` `` (grave) | Look around freely. The render camera rotates, but your player's actual yaw/pitch (where you aim, where your body faces) **never changes**. |
| **Yaw-Follow Look** | unbound by default | Switches to third-person (like pressing vanilla's F5) and swings the camera around as you look, but unlike vanilla F5, your **yaw continuously follows the camera** - so your character actually turns to face where you're looking, instead of staying fixed while just the camera orbits. |

Both keys default to **hold-to-activate**. Set `cameraModeToggle=true` / `yawFollowModeToggle=true`
in `config/omnilook-lite.properties` to make either one press-to-toggle instead.

Bind the Yaw-Follow key yourself in Controls (it's registered under the "Omnilook Lite" category,
just unbound out of the box so it doesn't collide with anything). Rebinding is respected —
input polling goes through `GameOptions.isPressed(KeyBinding)`, the real live-state method for
this mappings build, not a hardcoded key code.

## Requirements

- Minecraft 1.6.4
- [Legacy Fabric Loader](https://legacyfabric.net/)
- No Legacy Fabric API needed — this mod has zero dependency on it. Keybindings are registered
  directly against vanilla's `GameOptions.allKeys` array instead of going through a Fabric API
  keybinding module, so Fabric Loader alone is enough.

## Building
in CMD Prompt enter
```
gradlew.bat build
```

Output jar lands in `build/libs/`

## Notes on design 

- No config-screen (Mod Menu / Cloth Config) integration is wired up — settings live in
  `config/omnilook-lite.properties`, editable by hand. Wiring up Legacy Mod Menu is a
  reasonable follow-up if you want a GUI.
- Yaw-Follow mode switches `GameOptions.perspective` to third-person-behind (`1`) on activation
  (the same field vanilla's own F5 key drives, verified via the tiny mappings file) and restores
  whatever perspective you were in before once you release/toggle off the key. It continuously
  writes `yaw`/`pitch`/`prevYaw`/`prevPitch` back onto the player each frame it's active, which
  is what makes your character actually turn to face the camera instead of the camera just
  orbiting a fixed-facing character the way vanilla F5 does.

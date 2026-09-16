# Omnilook Lite (1.6.4 / Legacy Fabric)

A minimal, single-version freelook mod inspired by [Omnilook](https://github.com/rhysdh540/Omnilook),
built for Minecraft 1.6.4 on Legacy Fabric. This is **not** the real Omnilook mod/source -
it's a from-scratch implementation covering the behaviour you asked for, since Omnilook's
own source isn't distributed in a way I could pull down and repackage.

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

```
./gradlew build
```

Output jar lands in `build/libs/`.

## How class/method names were determined

Early drafts of this mod were written against *plausible* 1.6.4 class/method names based on
general Minecraft-modding convention, and that turned out wrong in several places — 1.6.4's
Legacy Yarn mappings actually use **modern yarn-style names** (`MinecraftClient`, not
`Minecraft`; `ClientPlayerEntity`, not `EntityClientPlayerMP`; `GameOptions`, not
`GameSettings`), and some members have no human-readable mapping at all yet (fields fall back to
a raw intermediary name like `field_3805`).

Every name actually used in this final version was **verified directly against this project's
real tiny-format mappings file** (`net.legacyfabric:yarn:1.6.4+build.604`, tiny v2, `named` /
`official` / `intermediary` columns) rather than guessed - both the class/method names
themselves, and (where it mattered) whether a field was public or had a real public accessor,
by cross-checking against yarn's field-visibility history across other Minecraft versions.

Two members turned out to have no safe way to access directly:

- `MinecraftClient`'s local-player field has no named mapping (`field_3805` only), so its real
  Java visibility can't be determined from the mappings file alone.
- `KeyBinding`'s `code`/`pressed` fields are confirmed **private** in every yarn mappings
  generation checked (this isn't 1.6.4-specific - it's been private since the earliest versions
  with named mappings at all).

For the first, `MinecraftClientAccessor.java` is a Mixin `@Accessor` interface that exposes
`field_3805` (the player) and `currentScreen` regardless of their actual visibility - this is
the standard, idiomatic Fabric technique for exactly this situation, and sidesteps needing to
know or guess the modifier. For the second, keybinding down-state is read through the real
public API instead: `GameOptions.isPressed(KeyBinding)` (a static method *on* `GameOptions`,
confirmed via the mappings file - not an instance method on `KeyBinding` itself, which was an
easy wrong turn to take).

**If you regenerate mappings later and something renames:** re-run the same lookup against the
new tiny file - `grep -P "^c\t" file.tiny` lists every class as `named  official  intermediary`,
and `awk` scoped between a class's `^c\t` line and the next one lists that class's fields/methods
the same way. `./gradlew genSources` plus an IDE also works if you'd rather browse decompiled
source directly.

## Notes on design

- No config-screen (Mod Menu / Cloth Config) integration is wired up — settings live in
  `config/omnilook-lite.properties`, editable by hand. Wiring up Legacy Mod Menu is a
  reasonable follow-up if you want a GUI.
- Camera Freelook mode reads raw LWJGL mouse deltas directly (rather than relying on
  vanilla's mouse handling) so it can apply them purely to the camera without any risk of
  them leaking into entity rotation. `MixinEntity` also blocks `Entity#setRotation` while this
  mode is active as a second layer of protection.
- Yaw-Follow mode switches `GameOptions.perspective` to third-person-behind (`1`) on activation
  (the same field vanilla's own F5 key drives, verified via the tiny mappings file) and restores
  whatever perspective you were in before once you release/toggle off the key. It continuously
  writes `yaw`/`pitch`/`prevYaw`/`prevPitch` back onto the player each frame it's active, which
  is what makes your character actually turn to face the camera instead of the camera just
  orbiting a fixed-facing character the way vanilla F5 does.

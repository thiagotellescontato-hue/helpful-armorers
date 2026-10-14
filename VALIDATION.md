# Minecraft 26.3 validation

Validation date: 2026-10-14

## Reported failure

GitHub issue #1 contains a Minecraft 26.3 dedicated-server crash with `NoSuchMethodError` for `Villager.swing(InteractionHand)` while Helpful Armorers 1.2.0 repairs an iron golem.

The Minecraft 26.3 bytecode uses `Villager.swing(InteractionHand, SwingAnimation)`. The 26.3 compatibility implementation calls that overload with `SwingAnimation.DEFAULT`; the 26.1 through 26.2 builds retain the one-argument call.

## Automated GameTest

Command:

```powershell
.\gradlew.bat -p mc26 clean build runGameTest --no-daemon --console=plain
```

Environment:

- Minecraft 26.3
- Fabric Loader 0.19.5
- Fabric API 0.161.0+26.3
- Java 25.0.2
- Helpful Armorers 1.2.1

Result: `BUILD SUCCESSFUL`; all 2 required tests passed. The Helpful Armorers test invokes the injected repair handler in a dedicated GameTest server and verifies that a nearby damaged iron golem is healed and that the armorer holds an iron ingot. Execution reaches the Minecraft 26.3 swing call without `NoSuchMethodError`.

## Dedicated server

Command:

```powershell
.\gradlew.bat -p mc26 runServer --no-daemon --console=plain
```

Result: the dedicated server loaded Helpful Armorers 1.2.1 with Minecraft 26.3, Fabric Loader 0.19.5, Fabric API 0.161.0+26.3, and Java 25.0.2. Startup reached `Done (0.309s)! For help, type "help"` and the server stopped cleanly with the `stop` command. No graphical client was started.

## Compatibility builds

All existing targets completed `clean build` successfully before the final 26.3 build:

- Minecraft 1.21.1 through 1.21.11 with Java 21
- Minecraft 26.1, 26.1.1, 26.1.2, and 26.2 with Java 25 and Helpful Armorers 1.2.0

## Final artifact

- Path: `jars/helpfularmorers-26.3-1.2.1.jar`
- SHA-256: `A968A99BD148BF72D438C40E3F6379EADD4961C07B918A14365F37D0368A14EB`
- Embedded mod version: `1.2.1`
- Embedded Minecraft dependency: `26.3`
- Embedded Java dependency: `>=25`
- Bytecode call: `Villager.swing(InteractionHand, SwingAnimation)` with `SwingAnimation.DEFAULT`

The SHA-256 is identical to `mc26/build/libs/helpfularmorers-26.3-1.2.1.jar`, the output of the validated clean build.

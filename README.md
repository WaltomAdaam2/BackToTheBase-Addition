# BackToTheBase

[English](README.md) | [简体中文](README.zh-CN.md)

A Xinbot plugin that triggers ender pearls to bring a bot back to base.

## Features
- Trigger a configured button to launch an ender pearl
- Per-user button location and facing direction
- Simple JSON configuration

## How to use
1. Install [MovementSync](https://github.com/huangdihd/movementsync).
2. Download the plugin JAR from the Releases page.
3. Place the JAR in your Xinbot plugins folder.
4. Place a button that can trigger the ender pearl where the bot can reach it.
5. Reload your Xinbot instance.
6. Edit `base_config.json` with your button coordinates and facing direction:

   ```json
   {
     "Steve": {
       "x": 100,
       "y": 64,
       "z": 200
     },
     "Alex": {
       "x": -50,
       "y": 70,
       "z": 30
     }
   }
   ```

   **Configuration details:**
   - The root object is a JSON map.
   - **Key** (e.g., `"Steve"`): The **In-game ID** of the player who will send the command.
   - **Value**: A coordinate object with `x`, `y`, and `z` representing the **exact block coordinates of the button**.
   - The bot automatically detects the button's facing direction. When it receives a `back` private message, it will find a safe path to the button, move to a reachable position, aim at it, and press it.
7. Reload your Xinbot instance again to apply the changes.
8. Send the bot a private message with the content `back`.

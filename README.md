# BackToTheBase

[English](README.md) | [简体中文](README.zh-CN.md)

A Xinbot plugin that triggers an ender pearl mechanism and brings the bot back to base by pressing a configured button.

## Features
- Trigger a configured button to launch an ender pearl
- Per-user button location and facing direction
- Simple JSON configuration
- Can otionally return the bot to a configured return location after pressing the pearl button.
- Automatically detect the button's facing direction
- Automatically find a reachable and safe standing position near the button before clicking it
- SimpleJSON configuration and automatically migrates old version's format to new `locations` format.

## How to use
1. Install [MovementSync](https://github.com/huangdihd/movementsync).
2. Download the plugin JAR from the Releases page.
3. Place the JAR in your Xinbot plugins folder.
4. Place a button that can trigger the ender pearl mechanism.
5. Make sure the bot can pathfind to a nearby position where the button can be clicked.
6. Start or reload your Xinbot instance.
7. Edit the generated `base_config.json` file.
8. Send the bot a private message with `back` or `back <number>`.

## Command Formats

| Private message | Description |
| --- | --- |
| `back` | Uses location number `1` for the player who sent the message. |
| `back 1` | Uses location number `1`. This is the same target as `back`. |
| `back 2` | Uses location number `2` if it exists in the player's configuration. |
| `back <number>` | Uses the matching configured location number. The number should be a positive integer. |

   **Configuration details:**
   - The root object is a JSON map.
   - **Key** (e.g., `"Steve"`): The **In-game ID** of the player who will send the command.
   - **Value**: A coordinate object with `x`, `y`, and `z` representing the **exact block coordinates of the button**.
   - The bot automatically detects the button's facing direction. When it receives a `back` private message, it will find a safe path to the button, move to a reachable position, aim at it, and press it.

## Configuration

`base_config.json` is a JSON object. Each key is a player's in-game ID, and each value is that player's BackToTheBase configuration.

Example:

```json
{
  "Steve": {
    "locations": [
      {
        "number": "1",
        "x": 100,
        "y": 64,
        "z": 200
      },
      {
        "number": "2",
        "x": 120,
        "y": 64,
        "z": 210
      }
    ],
    "returnAfterUse": false,
    "returnLocation": {
      "x": 100,
      "y": 64,
      "z": 200
    }
  },
  "Alex": {
    "locations": [
      {
        "number": "1",
        "x": -50,
        "y": 70,
        "z": 30
      }
    ],
    "returnAfterUse": true,
    "returnLocation": {
      "x": -45,
      "y": 70,
      "z": 35
    }
  }
}
```

### Configuration fields

| Field | Type | Description |
| --- | --- | --- |
| Player key | String | The in-game ID of the player who is allowed to trigger the bot. For example, `"Steve"`. |
| `locations` | Array | A list of button locations available to this player. |
| `locations[].number` | String | The location number used by the command. `"1"` is the default location used by `back`. |
| `locations[].x` | Number | The exact X coordinate of the button block. |
| `locations[].y` | Number | The exact Y coordinate of the button block. |
| `locations[].z` | Number | The exact Z coordinate of the button block. |
| `returnAfterUse` | Boolean | If `true`, the bot will click the pearl button first and then walk back to `returnLocation`. If `false`, it will stop after pressing the button. |
| `returnLocation` | Object | The position the bot should return to after pressing the button when `returnAfterUse` is enabled. |
| `returnLocation.x` | Number | Return position X coordinate. |
| `returnLocation.y` | Number | Return position Y coordinate. |
| `returnLocation.z` | Number | Return position Z coordinate. |

## Return Function

When `returnAfterUse` is set to `true`, the bot will follow this order:

1. Reload `base_config.json`.
2. Select the requested button location.
3. Walk to a safe reachable position near the button.
4. Look at the button and press it.
5. Walk back to `returnLocation`.

## Legacy configuration compatibility

Older versions used this simpler format:

```json
{
  "Steve": {
    "x": 100,
    "y": 64,
    "z": 200
  }
}
```

This format is still supported for compatibility. When the plugin detects an old configuration entry, it will convert it to the new `locations` format automatically. The old `x`, `y`, and `z` values become location number `1`.

After migration, the configuration format should look like the example configuration above.

## Building from source

This project uses Maven.

```bash
mvn clean package
```
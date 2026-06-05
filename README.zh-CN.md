# BackToTheBase

[English](README.md) | [简体中文](README.zh-CN.md)

一个 Xinbot 插件，可以通过按下配置好的按钮来触发末影珍珠装置，并将机器人带回基地。

## 功能

* 能触发配置好的按钮位置来实现珍珠超传
* 支持为不同玩家配置独立，多个的按钮位置和朝向检测
* 使用简单的 JSON 配置文件
* 可以设置在按下珍珠按钮后，让机器人自动返回到配置好的返回位置
* 自动检测按钮的位置，朝向，以及最优路线
* 在点击按钮前，自动寻找按钮附近可到达且安全的站立位置
* 拥有简单的 JSON 配置，并且可以自动将旧版本配置格式迁移到新的 `locations` 格式

## 使用方法

1. 安装 [MovementSync](https://github.com/huangdihd/movementsync)。
2. 从 Releases 页面下载插件 JAR。
3. 将 JAR 放入 Xinbot 的 plugins 文件夹。
4. 放置一个可以触发末影珍珠装置的按钮。
5. 确保机器人可以寻路到按钮附近，并且能够站在一个可以点击到按钮的位置。
6. 启动或重载 Xinbot 实例。
7. 编辑自动生成的 `base_config.json` 文件。
8. 向机器人发送私聊消息 `back` 或 `back <number>`。

## 指令格式

| 私聊消息            | 说明                              |
| --------------- | ------------------------------- |
| `back`          | 触发该玩家配置中的 `1` 号珍珠点位。              |
| `back 1`        | 触发 `1` 号珍珠点位，效果与 `back` 相同。       |
| `back 2`        | 如果该玩家配置中存在 `2` 号位置，则使用 `2` 号位置。 |
| `back <number>` | 使用与该编号匹配的配置位置。编号应为正整数。          |

**配置说明：**

* **玩家名**，例如 `"Steve"`：代表可以发送指令的玩家的**游戏内 ID**。
* **权重数字**：是该玩家的 BackToTheBase 配置。
* 按钮坐标中的 `x`、`y`、`z` 代表**按钮方块本身的精确方块坐标**。
* 机器人会自动检测按钮的朝向。当它收到 `back` 私聊消息时，会自动寻找一条安全路径，移动到按钮附近可点击的位置，瞄准按钮并按下它。

## 配置

`base_config.json` 是一个 JSON 对象。每个键都是一个玩家的游戏内 ID，每个值都是该玩家对应的 BackToTheBase 配置。

示例：

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

### 配置字段

| 字段                   | 类型      | 说明                                                                     |
| -------------------- | ------- | ---------------------------------------------------------------------- |
| 玩家键名                 | String  | 允许触发机器人的玩家游戏内 ID，例如 `"Steve"`。                                         |
| `locations`          | Array   | 该玩家可用的按钮位置列表。                                                          |
| `locations[].number` | String  | 指令使用的位置编号。`"1"` 是 `back` 默认使用的位置。                                      |
| `locations[].x`      | Number  | 按钮方块的精确 X 坐标。                                                          |
| `locations[].y`      | Number  | 按钮方块的精确 Y 坐标。                                                          |
| `locations[].z`      | Number  | 按钮方块的精确 Z 坐标。                                                          |
| `returnAfterUse`     | Boolean | 如果为 `true`，机器人会先点击珍珠按钮，然后走回 `returnLocation`。如果为 `false`，机器人会在按下按钮后停止，直到再次收到珍珠消息。 |
| `returnLocation`     | Object  | 当启用 `returnAfterUse` 时，机器人按下按钮后要返回的位置。                                 |
| `returnLocation.x`   | Number  | 珍珠点位的 X 坐标。                                                            |
| `returnLocation.y`   | Number  | 珍珠点位的 Y 坐标。                                                            |
| `returnLocation.z`   | Number  | 珍珠点位的 Z 坐标。                                                            |

## 返回功能

当 `returnAfterUse` 设置为 `true` 时，机器人会按照以下顺序执行：

1. 重新加载 `base_config.json`。
2. 选择指令指定的按钮位置。
3. 移动到按钮附近安全且可到达的位置。
4. 瞄准按钮并按下按钮。
5. 走回 `returnLocation`。

## 旧版配置兼容

旧版本使用的是更简单的配置格式：

```json
{
  "Steve": {
    "x": 100,
    "y": 64,
    "z": 200
  }
}
```

为了兼容旧版本，该格式仍然可以使用。

当插件检测到旧格式的配置项时，会自动将其转换为新的 `locations` 格式。旧配置中的 `x`、`y`、`z` 会变成编号为 `"1"` 的按钮位置。迁移完成后，配置格式应与上方的新配置示例类似。

## 从源码构建

本项目使用 Maven。

```bash
mvn clean package
```
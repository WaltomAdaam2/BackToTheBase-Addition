package huangdihd.xinbot;

import java.util.Locale;

public class BackToTheBaseLanguage {
    public static final String ENGLISH = "English";
    public static final String CHINESE = "Chinese";

    private final boolean chinese;

    private BackToTheBaseLanguage(String language) {
        this.chinese = CHINESE.equals(normalize(language));
    }

    public static BackToTheBaseLanguage of(String language) {
        return new BackToTheBaseLanguage(language);
    }

    public static boolean isValid(String language) {
        return ENGLISH.equalsIgnoreCase(language) || CHINESE.equalsIgnoreCase(language);
    }

    public static String normalize(String language) {
        if (ENGLISH.equalsIgnoreCase(language)) {
            return ENGLISH;
        }
        if (CHINESE.equalsIgnoreCase(language)) {
            return CHINESE;
        }
        return CHINESE;
    }

    public static String detectSystemDefault() {
        String language = Locale.getDefault().getLanguage();
        return language != null && language.toLowerCase(Locale.ROOT).startsWith("zh") ? CHINESE : ENGLISH;
    }

    public String unknownCommand() {
        return prefix() + (chinese ? "未知命令。" : "Unknown command.");
    }

    public String saveFailed() {
        return prefix() + (chinese ? "配置保存失败，请检查日志。" : "Failed to save config. Please check the log.");
    }

    public String consoleOnly() {
        return prefix() + (chinese ? "该命令只能在控制台使用。" : "This command can only be used from the console.");
    }

    public String usage(String usage) {
        return prefix() + (chinese ? "用法: " : "Usage: ") + usage;
    }

    public String languageChanged(String language) {
        return prefix() + (chinese
                ? "语言已切换为 " + language + "。"
                : "Language changed to " + language + ".");
    }

    public String returnEnabled(boolean enabled) {
        if (chinese) {
            return prefix() + (enabled ? "已开启返回功能。" : "已关闭返回功能。");
        }
        return prefix() + (enabled ? "Return after use enabled." : "Return after use disabled.");
    }

    public String coordinatesMustBeIntegers() {
        return prefix() + (chinese ? "坐标必须是整数。" : "Coordinates must be integers.");
    }

    public String numberAndCoordinatesMustBeIntegers() {
        return prefix() + (chinese ? "编号和坐标必须是整数。" : "Number and coordinates must be integers.");
    }

    public String returnPointSet(int x, int y, int z) {
        if (chinese) {
            return prefix() + "已设置返回坐标为 " + x + " " + y + " " + z + "。";
        }
        return prefix() + "Return location set to " + x + " " + y + " " + z + ".";
    }

    public String playerExists(String playerName) {
        return prefix() + (chinese ? "玩家 " + playerName + " 已存在。" : "Player " + playerName + " already exists.");
    }

    public String playerMissing(String playerName) {
        return prefix() + (chinese ? "玩家 " + playerName + " 不存在。" : "Player " + playerName + " does not exist.");
    }

    public String playerAdded(String playerName) {
        return prefix() + (chinese ? "已添加玩家 " + playerName + "。" : "Added player " + playerName + ".");
    }

    public String waitingRemovePlayer(String playerName, String confirmCommand) {
        if (chinese) {
            return prefix() + "等待确认删除玩家 " + playerName + "。请输入 " + confirmCommand + " 确认。";
        }
        return prefix() + "Waiting to confirm removing player " + playerName + ". Run " + confirmCommand + " to confirm.";
    }

    public String locationMissing(String number) {
        return prefix() + (chinese ? "珍珠坐标 " + number + " 不存在。" : "Pearl button location " + number + " does not exist.");
    }

    public String locationNotConfigured(String number) {
        return prefix() + (chinese ? "珍珠坐标 " + number + " 未配置。" : "Pearl button location " + number + " is not configured.");
    }

    public String cannotRemoveLastLocation(String playerName) {
        if (chinese) {
            return prefix() + "不能删除 " + playerName + " 的最后一个珍珠坐标，请使用 player remove。";
        }
        return prefix() + "Cannot remove " + playerName + "'s last pearl button location. Use player remove.";
    }

    public String waitingRemoveLocation(String playerName, String number, String confirmCommand) {
        if (chinese) {
            return prefix() + "等待确认删除 " + playerName + " 的珍珠坐标 " + number + "。请输入 " + confirmCommand + " 确认。";
        }
        return prefix() + "Waiting to confirm removing " + playerName + "'s pearl button location " + number + ". Run " + confirmCommand + " to confirm.";
    }

    public String locationExists(String number) {
        if (chinese) {
            return prefix() + "珍珠坐标 " + number + " 已存在，请使用 loc set 覆盖。";
        }
        return prefix() + "Pearl button location " + number + " already exists. Use loc set to replace it.";
    }

    public String locationAddResult(boolean createdPlayer, String playerName, String number, int x, int y, int z) {
        if (chinese) {
            return prefix() + (createdPlayer
                    ? "已创建玩家 " + playerName + "，并添加珍珠坐标 " + number + ": " + coords(x, y, z) + "。"
                    : "已为 " + playerName + " 添加珍珠坐标 " + number + ": " + coords(x, y, z) + "。");
        }
        return prefix() + (createdPlayer
                ? "Created player " + playerName + " and added pearl button location " + number + ": " + coords(x, y, z) + "."
                : "Added pearl button location " + number + " for " + playerName + ": " + coords(x, y, z) + ".");
    }

    public String locationSetCreatedPlayer(String playerName, String number, int x, int y, int z) {
        if (chinese) {
            return prefix() + "已创建玩家 " + playerName + "，并设置珍珠坐标 " + number + ": " + coords(x, y, z) + "。";
        }
        return prefix() + "Created player " + playerName + " and set pearl button location " + number + ": " + coords(x, y, z) + ".";
    }

    public String locationSetResult(boolean existed, String playerName, String number, int x, int y, int z) {
        if (chinese) {
            return prefix() + (existed
                    ? "已更新 " + playerName + " 的珍珠坐标 " + number + " 为 " + coords(x, y, z) + "。"
                    : "已创建 " + playerName + " 的珍珠坐标 " + number + ": " + coords(x, y, z) + "。");
        }
        return prefix() + (existed
                ? "Updated " + playerName + "'s pearl button location " + number + " to " + coords(x, y, z) + "."
                : "Created " + playerName + "'s pearl button location " + number + ": " + coords(x, y, z) + ".");
    }

    public String adminExists(String playerName) {
        return prefix() + (chinese ? "管理员 " + playerName + " 已存在。" : "Admin " + playerName + " already exists.");
    }

    public String adminMissing(String playerName) {
        return prefix() + (chinese ? "管理员 " + playerName + " 不存在。" : "Admin " + playerName + " does not exist.");
    }

    public String adminLimit(int maxAdmins, String playerName) {
        if (chinese) {
            return prefix() + "管理员数量已达到上限 " + maxAdmins + "，无法添加 " + playerName + "。";
        }
        return prefix() + "Admin limit " + maxAdmins + " reached. Cannot add " + playerName + ".";
    }

    public String adminAdded(String playerName) {
        return prefix() + (chinese ? "已添加管理员 " + playerName + "。" : "Added admin " + playerName + ".");
    }

    public String adminRemoved(String playerName) {
        return prefix() + (chinese ? "已删除管理员 " + playerName + "。" : "Removed admin " + playerName + ".");
    }

    public String adminEnabled(boolean enabled) {
        if (chinese) {
            return prefix() + (enabled ? "已开启游戏内管理。" : "已关闭游戏内管理。");
        }
        return prefix() + (enabled ? "In-game admin commands enabled." : "In-game admin commands disabled.");
    }

    public String noPendingAction() {
        return prefix() + (chinese ? "没有需要确认的操作。" : "There is no pending action to confirm.");
    }

    public String pendingExpired() {
        return prefix() + (chinese ? "确认操作已超时，请重新执行删除命令。" : "The confirmation timed out. Please run the remove command again.");
    }

    public String confirmedRemovePlayer(String playerName) {
        return prefix() + (chinese ? "已确认，删除玩家 " + playerName + "。" : "Confirmed. Removed player " + playerName + ".");
    }

    public String confirmedRemoveLocation(String playerName, String number) {
        if (chinese) {
            return prefix() + "已确认，删除 " + playerName + " 的珍珠坐标 " + number + "。";
        }
        return prefix() + "Confirmed. Removed " + playerName + "'s pearl button location " + number + ".";
    }

    public String statusHeader() {
        return consolePrefix() + "===== BackToTheBase " + (chinese ? "状态" : "Status") + " =====";
    }

    public String returnFeature(boolean enabled) {
        if (chinese) {
            return consolePrefix() + "返回功能: " + (enabled ? "运行中 (配置: 启用)" : "已停止 (配置: 禁用)");
        }
        return consolePrefix() + "Return: " + (enabled ? "running (config: enabled)" : "stopped (config: disabled)");
    }

    public String returnLocation(int x, int y, int z) {
        return consolePrefix() + (chinese ? "返回坐标: " : "Return location: ") + coords(x, y, z);
    }

    public String adminFeature(boolean enabled) {
        if (chinese) {
            return consolePrefix() + "游戏内管理: " + (enabled ? "运行中 (配置: 启用)" : "已停止 (配置: 禁用)");
        }
        return consolePrefix() + "In-game admin: " + (enabled ? "running (config: enabled)" : "stopped (config: disabled)");
    }

    public String adminCount(int count, int maxAdmins) {
        return consolePrefix() + (chinese ? "管理员数量: " : "Admins: ") + count + " / " + maxAdmins;
    }

    public String playerCount(int count) {
        return consolePrefix() + (chinese ? "玩家数量: " : "Players: ") + count;
    }

    public String locationCount(int count) {
        return consolePrefix() + (chinese ? "珍珠坐标数量: " : "Pearl button locations: ") + count;
    }

    public String playerDataHeader() {
        return consolePrefix() + (chinese ? "玩家数据:" : "Player data:");
    }

    public String playerDataLine(String playerName, int locationCount) {
        return consolePrefix() + "  " + playerName + ": " + locationCount + (chinese ? " 个珍珠坐标" : " pearl button locations");
    }

    public String divider() {
        return consolePrefix() + "================================";
    }

    public String gameStatus(boolean returnEnabled, int x, int y, int z, boolean adminEnabled, int admins, int maxAdmins, int players, int locations) {
        if (chinese) {
            return prefix() + "状态: 返回功能=" + enabledText(returnEnabled) + ", 返回坐标=" + coords(x, y, z)
                    + ", 游戏内管理=" + enabledText(adminEnabled) + ", 管理员=" + admins + "/" + maxAdmins
                    + ", 玩家=" + players + ", 珍珠坐标=" + locations;
        }
        return prefix() + "Status: return=" + enabledText(returnEnabled) + ", return location=" + coords(x, y, z)
                + ", in-game admin=" + enabledText(adminEnabled) + ", admins=" + admins + "/" + maxAdmins
                + ", players=" + players + ", pearl locations=" + locations;
    }

    public String playerListHeader() {
        return consolePrefix() + "===== BackToTheBase " + (chinese ? "玩家列表" : "Players") + " =====";
    }

    public String noPlayers() {
        return prefix() + (chinese ? "暂无玩家配置。" : "No player configs.");
    }

    public String noPlayersConsole() {
        return consolePrefix() + (chinese ? "暂无玩家配置。" : "No player configs.");
    }

    public String playerLocationData(String playerName, String numbers) {
        return consolePrefix() + "  " + playerName + ": " + (chinese ? "珍珠坐标 " : "pearl locations ") + numbers;
    }

    public String gamePlayerList(String players) {
        return prefix() + (chinese ? "玩家列表: " : "Players: ") + players;
    }

    public String gamePlayerEntry(String playerName, String numbers) {
        return playerName + (chinese ? "(珍珠坐标 " : "(pearl locations ") + numbers + ")";
    }

    public String locationListHeader(String playerName) {
        return consolePrefix() + "===== " + playerName + (chinese ? " 的珍珠坐标" : "'s pearl button locations") + " =====";
    }

    public String locationLine(String number, int x, int y, int z) {
        return consolePrefix() + "  " + number + ": " + coords(x, y, z);
    }

    public String gameLocationList(String playerName, String locations) {
        return prefix() + playerName + (chinese ? " 的珍珠坐标: " : "'s pearl button locations: ") + locations;
    }

    public String enabledText(boolean enabled) {
        if (chinese) {
            return enabled ? "启用" : "禁用";
        }
        return enabled ? "enabled" : "disabled";
    }

    public String movementSyncDisabled() {
        return prefix() + (chinese ? "MovementSync 未启用，无法执行 back 命令。" : "MovementSync is not enabled. Cannot run back.");
    }

    public String movementSyncInvalid() {
        return prefix() + (chinese ? "MovementSync 插件实例异常，无法执行 back 命令。" : "MovementSync plugin instance is invalid. Cannot run back.");
    }

    public String actionAlreadyRunning() {
        return prefix() + (chinese ? "已有拉珍珠任务正在运行。" : "A BackToTheBase action is already running.");
    }

    public String backStarted(String number) {
        return prefix() + (chinese ? "正在拉动珍珠坐标 " + number + "。" : "Pulling pearl button location " + number + ".");
    }

    private static String prefix() {
        return "[BackToTheBase] ";
    }

    private static String consolePrefix() {
        return "[BackToTheBase]: ";
    }

    private static String coords(int x, int y, int z) {
        return x + " " + y + " " + z;
    }
}

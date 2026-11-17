import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.HangingSign;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MainClass extends JavaPlugin implements Listener {

    private final Map<UUID, Long> cooldowns = new HashMap<>();

    private static final long COMMAND_COOLDOWN = 3000L;
    private static final String LINE2 = "plcmd";

    @Override
    public void onLoad() {
        getLogger().info("PLCMD木牌正在加载");
    }

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("PLCMD木牌已启动");
    }

    @Override
    public void onDisable() {
        cooldowns.clear();
        getLogger().info("PLCMD木牌已关闭");
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {

        // 只处理右键方块
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (event.useInteractedBlock() != Event.Result.ALLOW) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }

        Sign sign = null;
        if (block.getState() instanceof Sign s) {
            sign = s;
        } else if (block.getState() instanceof HangingSign hs) {
            sign = hs;
        }
        if (sign == null) {
            return;
        }

        SignSide front = sign.getSide(Side.FRONT);
        SignSide back = sign.getSide(Side.BACK);

        String[] lines = null;
        if (front.getLines()[1].trim().equalsIgnoreCase(LINE2)) {
            lines = front.getLines();
        } else if (back.getLines()[1].trim().equalsIgnoreCase(LINE2)) {
            lines = back.getLines();
        }
        if (lines == null) return;

        // 检查第二行标识
        String secondLine = lines[1].trim().toLowerCase();

        if (!LINE2.equals(secondLine)) {
            return;
        }

        // 拼接第三行和第四行命令
        String cmd = (lines[2] + lines[3]).trim();

        if (cmd.isEmpty()) {
            return;
        }

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // 冷却检查
        long now = System.currentTimeMillis();
        long lastUse = cooldowns.getOrDefault(uuid, 0L);
        long remaining = COMMAND_COOLDOWN - (now - lastUse);

        if (remaining > 0) {
            long secondsLeft = (remaining + 999) / 1000;
            player.sendMessage(ChatColor.RED + "请等待" + secondsLeft + "秒后尝试!");
            return;
        }

        // 更新冷却时间
        cooldowns.put(uuid, now);

        // 触发命令预处理事件
        PlayerCommandPreprocessEvent commandEvent =
                new PlayerCommandPreprocessEvent(player, "/" + cmd);

        Bukkit.getPluginManager().callEvent(commandEvent);

        if (commandEvent.isCancelled()) {
            return;
        }

        // 执行命令
        String finalCommand = commandEvent.getMessage();

        if (finalCommand.startsWith("/")) {
            finalCommand = finalCommand.substring(1);
        }

        player.performCommand(finalCommand);
    }

    // 玩家退出时清理冷却缓存
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        cooldowns.remove(event.getPlayer().getUniqueId());
    }
}

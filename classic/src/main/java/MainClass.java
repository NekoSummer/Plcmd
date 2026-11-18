import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class MainClass extends JavaPlugin implements Listener {

    private final Map<String, Long> cooldowns = new HashMap<>();
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
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.isCancelled()) return;

        Block block = event.getClickedBlock();
        if (block == null) return;

        Material mat = block.getType();
        if (mat != Material.SIGN_POST && mat != Material.WALL_SIGN) {
            return;
        }

        // 现在再拿state，此时才安全
        if (!(block.getState() instanceof Sign)) return;
        Sign sign = (Sign) block.getState();

        String[] lines = sign.getLines();
        String secondLine = lines[1].trim().toLowerCase();
        if (!LINE2.equals(secondLine)) return;

        String cmd = (lines[2] + lines[3]).trim();
        if (cmd.isEmpty()) return;

        Player player = event.getPlayer();
        String pName = player.getName();

        long now = System.currentTimeMillis();
        long lastUse = 0L;

        if (cooldowns.containsKey(pName)) lastUse = cooldowns.get(pName);

        long remaining = COMMAND_COOLDOWN - (now - lastUse);

        if (remaining > 0) {
            long secondsLeft = (remaining + 999) / 1000;
            player.sendMessage(ChatColor.RED + "请等待" + secondsLeft + "秒后尝试!");
            return;
        }

        cooldowns.put(pName, now);

        PlayerCommandPreprocessEvent cmdEvent =
                new PlayerCommandPreprocessEvent(player, "/" + cmd);

        Bukkit.getPluginManager().callEvent(cmdEvent);
        if (cmdEvent.isCancelled()) return;

        String finalCmd = cmdEvent.getMessage();
        if (finalCmd.startsWith("/")) finalCmd =finalCmd.substring(1);

        player.performCommand(finalCmd);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        cooldowns.remove(event.getPlayer().getName());
    }
}

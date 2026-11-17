import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MainClass extends JavaPlugin implements Listener {

    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private static final long commandCooldown = 3_000L;
    private static final String line2 = "plcmd";

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("PLCMD木牌已启动");
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (event.isCancelled()) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }

        Material type = block.getType();
        if (type != Material.SIGN && type != Material.WALL_SIGN) {
            return;
        }

        Sign sign = (Sign) block.getState();
        String[] lines = sign.getLines();

        String secondLine = lines.length > 1 ? lines[1].trim().toLowerCase() : "";
        if (!line2.equals(secondLine)) {
            return;
        }

        String cmd = "";
        if (lines.length > 2) cmd += lines[2].trim();
        if (lines.length > 3) cmd += lines[3].trim();

        if (cmd.isEmpty()) {
            return;
        }

        Player player = event.getPlayer();

        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();
        if (cooldowns.containsKey(uuid)) {
            long lastUse = cooldowns.get(uuid);
            if ((now - lastUse) < commandCooldown) {
                long secondsLeft = (commandCooldown - (now - lastUse)) / 1000 + 1;
                player.sendMessage(ChatColor.RED + "请等待" + secondsLeft + "秒后尝试!");
                return;
            }
        }
        cooldowns.put(uuid, now);

        player.performCommand(cmd);
    }
}

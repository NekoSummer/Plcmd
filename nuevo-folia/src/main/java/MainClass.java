import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
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
        LegacyComponentSerializer legacy = LegacyComponentSerializer.legacySection();

        String cmdContent = null;
        // line(1) = 第二行，索引从0开始
        String frontLine2 = legacy.serialize(front.line(1)).trim();
        if (frontLine2.equalsIgnoreCase(LINE2)) {
            // 拼接第3、4行
            String l3 = legacy.serialize(front.line(2));
            String l4 = legacy.serialize(front.line(3));
            cmdContent = (l3 + l4).trim();
        } else {
            String backLine2 = legacy.serialize(back.line(1)).trim();
            if (backLine2.equalsIgnoreCase(LINE2)) {
                String l3 = legacy.serialize(back.line(2));
                String l4 = legacy.serialize(back.line(3));
                cmdContent = (l3 + l4).trim();
            }
        }

        if (cmdContent == null || cmdContent.isBlank()) {
            return;
        }
        String cmd = cmdContent;

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // 冷却检查
        long now = System.currentTimeMillis();
        long lastUse = cooldowns.getOrDefault(uuid, 0L);
        long remaining = COMMAND_COOLDOWN - (now - lastUse);

        if (remaining > 0) {
            long secondsLeft = (remaining + 999) / 1000;
            player.sendMessage(Component.text("请等待" + secondsLeft + "秒后尝试!").color(NamedTextColor.RED));
            return;
        }

        // 更新冷却时间
        cooldowns.put(uuid, now);

        // 执行命令
        player.getScheduler().run(this, unused -> {
            PlayerCommandPreprocessEvent commandEvent = new PlayerCommandPreprocessEvent(player, "/" + cmd);
            Bukkit.getPluginManager().callEvent(commandEvent);
            if (commandEvent.isCancelled()) return;

            String finalCommand = commandEvent.getMessage();
            if (finalCommand.startsWith("/")) {
                finalCommand = finalCommand.substring(1);
            }
            player.performCommand(finalCommand);
        }, null);
    }

    // 玩家退出时清理冷却缓存
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        cooldowns.remove(event.getPlayer().getUniqueId());
    }
}

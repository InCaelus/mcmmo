package org.adelio.mcMMO.listener;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.adelio.mcMMO.data.PlayerData;
import org.adelio.mcMMO.data.PlayerManager;
import org.adelio.mcMMO.skill.SkillType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public class ExcavationListener implements Listener {
    private final PlayerManager playerManager;
    public ExcavationListener(PlayerManager pm) { this.playerManager = pm; }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player p = event.getPlayer();
        Material tool = p.getInventory().getItemInMainHand().getType();

        if (!tool.name().endsWith("_SHOVEL")) return;

        if (!isDiggable(event.getBlock().getType())) return;

        PlayerData data = playerManager.getPlayerData(p.getUniqueId());
        if (data == null) return;

        int level = data.getLevel(SkillType.EXCAVATION);

        double xp = 0.8;
        data.addXp(SkillType.EXCAVATION, xp);
        sendActionBar(p, SkillType.EXCAVATION, xp, data);

        if (Math.random() < (level * 0.015)) {
            dropTreasure(event.getBlock().getLocation(), level);
        }
    }

    private void dropTreasure(Location loc, int level) {
        double r = Math.random();
        Material t;
        if (level >= 25 && r < 0.05) t = Material.DIAMOND;
        else if (level >= 15 && r < 0.15) t = Material.GOLD_NUGGET;
        else t = Material.FLINT;
        loc.getWorld().dropItemNaturally(loc, new ItemStack(t));
    }

    private boolean isDiggable(Material m) {
        String n = m.name();
        return n.contains("DIRT") || n.contains("SAND") || n.equals("GRAVEL") ||
                n.equals("CLAY") || n.equals("GRASS_BLOCK"); // 잔디 추가
    }

    private void sendActionBar(Player p, SkillType t, double g, PlayerData d) {
        String msg = String.format("§e[%s] §f+%.1f XP §7(%.1f/%.1f)", t.getName(), g, d.getExp(t), d.getLevel(t)*100.0);
        p.sendActionBar(LegacyComponentSerializer.legacySection().deserialize(msg));
    }
}
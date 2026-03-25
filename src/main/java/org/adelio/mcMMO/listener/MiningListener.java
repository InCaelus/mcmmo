package org.adelio.mcMMO.listener;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.adelio.mcMMO.data.PlayerData;
import org.adelio.mcMMO.data.PlayerManager;
import org.adelio.mcMMO.skill.SkillType;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;

public class MiningListener implements Listener {
    private final PlayerManager playerManager;

    public MiningListener(PlayerManager pm) {
        this.playerManager = pm;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player p = event.getPlayer();
        ItemStack tool = p.getInventory().getItemInMainHand();

        if (!tool.getType().name().endsWith("_PICKAXE")) return;

        Block block = event.getBlock();
        Material mat = block.getType();

        if (!isMineable(mat)) return;

        PlayerData data = playerManager.getPlayerData(p.getUniqueId());
        if (data == null) return;

        double xp = getMiningXp(mat);
        data.addXp(SkillType.MINING, xp);
        sendActionBar(p, SkillType.MINING, xp, data);

        int level = data.getLevel(SkillType.MINING);

        if (level >= 30) {
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        if (x == 0 && y == 0 && z == 0) continue;
                        Block relative = block.getRelative(x, y, z);
                        if (isMineable(relative.getType())) {
                            relative.breakNaturally(tool);
                        }
                    }
                }
            }
        }

        boolean hasSilkTouch = tool.containsEnchantment(Enchantment.SILK_TOUCH);

        if (!hasSilkTouch && Math.random() < (level * 0.01)) {

            Collection<ItemStack> drops = block.getDrops(tool);
            for (ItemStack drop : drops) {

                if (!drop.getType().isBlock() || mat.name().contains("ORE")) {
                    block.getWorld().dropItemNaturally(block.getLocation(), drop);
                }
            }
        }
    }

    private double getMiningXp(Material m) {
        return switch (m) {
            case ANCIENT_DEBRIS -> 50.0;
            case DIAMOND_ORE, DEEPSLATE_DIAMOND_ORE, EMERALD_ORE, DEEPSLATE_EMERALD_ORE -> 20.0;
            case GOLD_ORE, DEEPSLATE_GOLD_ORE -> 10.0;
            case IRON_ORE, DEEPSLATE_IRON_ORE -> 8.0;
            case COAL_ORE, DEEPSLATE_COAL_ORE, LAPIS_ORE, DEEPSLATE_LAPIS_ORE -> 5.0;
            case NETHER_GOLD_ORE, NETHER_QUARTZ_ORE -> 3.0;
            case STONE, DEEPSLATE, TUFF, NETHERRACK -> 0.1;
            default -> 0.5;
        };
    }

    private boolean isMineable(Material m) {
        String n = m.name();
        return n.contains("ORE") || n.contains("STONE") || n.contains("DEEPSLATE") ||
                n.equals("NETHERRACK") || n.equals("TUFF") || n.equals("OBSIDIAN");
    }

    private void sendActionBar(Player p, SkillType t, double g, PlayerData d) {
        String msg = String.format("§b[%s] §f+%.1f XP §7(%.1f/%.1f)",
                t.getName(), g, d.getExp(t), d.getLevel(t) * 100.0);
        p.sendActionBar(LegacyComponentSerializer.legacySection().deserialize(msg));
    }
}
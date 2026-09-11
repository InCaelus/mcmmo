package org.adelio.mcMMO.listener;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.adelio.mcMMO.data.PlayerData;
import org.adelio.mcMMO.data.PlayerManager;
import org.adelio.mcMMO.skill.SkillType;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;

public class CombatListener implements Listener {
    private final PlayerManager playerManager;
    public CombatListener(PlayerManager pm) { this.playerManager = pm; }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player p)) return;

        Material tool = p.getInventory().getItemInMainHand().getType();
        if (!tool.name().endsWith("_SWORD")) return;

        PlayerData data = playerManager.getPlayerData(p.getUniqueId());
        if (data == null) return;

        int lvl = data.getLevel(SkillType.COMBAT);

        double damage = event.getDamage() + (lvl * 0.5);

        if (Math.random() < (lvl * 0.01)) {
            damage *= 1.5;
            p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1f, 1f);
        }

        event.setDamage(damage);

        double hitXp = event.getFinalDamage() * 0.2;
        data.addXp(SkillType.COMBAT, hitXp);
    }

    @EventHandler
    public void onKill(EntityDeathEvent event) {
        LivingEntity victim = event.getEntity();
        Player killer = victim.getKiller();

        if (killer == null) return;

        Material tool = killer.getInventory().getItemInMainHand().getType();
        if (!tool.name().endsWith("_SWORD")) return;

        PlayerData data = playerManager.getPlayerData(killer.getUniqueId());
        if (data == null) return;

        double killXp = switch (victim.getType()) {
            case ENDER_DRAGON -> 1000.0;
            case WITHER -> 300.0;
            case WARDEN -> 200.0;
            case ENDERMAN -> 15.0;
            case BLAZE, GHAST -> 12.0;
            case ZOMBIE, SKELETON, CREEPER -> 5.0;
            case SPIDER, CAVE_SPIDER -> 4.0;
            case SLIME, MAGMA_CUBE -> 2.0;
            case CHICKEN, PIG, COW, SHEEP -> 0.5;
            default -> 1.0;
        };

        data.addXp(SkillType.COMBAT, killXp);
        sendActionBar(killer, SkillType.COMBAT, killXp, data);
    }

    private void sendActionBar(Player p, SkillType t, double g, PlayerData d) {
        String msg = String.format("§c[%s] §f+%.1f XP §7(%.1f/%.1f)", t.getName(), g, d.getExp(t), d.getLevel(t)*100.0);
        p.sendActionBar(LegacyComponentSerializer.legacySection().deserialize(msg));
    }
}
package org.adelio.mcMMO.listener;

import net.kyori.adventure.text.Component;
import org.adelio.mcMMO.data.DataManager;
import org.adelio.mcMMO.data.PlayerData;
import org.adelio.mcMMO.skill.SkillType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class StatListener implements Listener {
    private final DataManager dataManager;
    private static final String GUI_TITLE = "§8[ mcMMO 캐릭터 정보 ]";

    public StatListener(DataManager dataManager) { this.dataManager = dataManager; }

    @EventHandler
    public void onShiftF(PlayerSwapHandItemsEvent event) {
        Player p = event.getPlayer();
        if (p.isSneaking()) {
            event.setCancelled(true);
            openGUI(p);
        }
    }

    public void openGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 45, Component.text(GUI_TITLE));
        PlayerData data = dataManager.getPlayerManager().getPlayerData(player.getUniqueId());
        if (data == null) return;

        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta gMeta = glass.getItemMeta();
        gMeta.displayName(Component.text(" "));
        glass.setItemMeta(gMeta);
        for (int i = 0; i < 45; i++) gui.setItem(i, glass);

        gui.setItem(13, createInfoItem(player.getName()));

        gui.setItem(28, createSkillItem(Material.DIAMOND_SWORD, "전투", data, SkillType.COMBAT));
        gui.setItem(30, createSkillItem(Material.DIAMOND_PICKAXE, "채광", data, SkillType.MINING));
        gui.setItem(32, createSkillItem(Material.DIAMOND_AXE, "벌목", data, SkillType.WOODCUTTING));
        gui.setItem(34, createSkillItem(Material.DIAMOND_SHOVEL, "삽질", data, SkillType.EXCAVATION));

        player.openInventory(gui);
    }

    private ItemStack createInfoItem(String name) {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("§b§l" + name + "님의 모험 요약"));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("§7현재 습득한 기술 레벨을 확인합니다."));
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createSkillItem(Material m, String name, PlayerData data, SkillType type) {
        ItemStack item = new ItemStack(m);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("§e§l" + name + " 스탯"));

        List<Component> lore = new ArrayList<>();
        int level = data.getLevel(type);
        double currentExp = data.getExp(type);
        double maxExp = level * 100.0;

        lore.add(Component.text("§f현재 레벨: §a" + level));
        lore.add(Component.text("§f경험치: §7" + String.format("%.1f", currentExp) + " / " + maxExp));

        lore.add(Component.text(getProgressBar(currentExp, maxExp)));

        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private String getProgressBar(double current, double max) {
        int bars = 10;
        int filled = (int) ((current / max) * bars);
        StringBuilder sb = new StringBuilder("§8[");
        for (int i = 0; i < bars; i++) {
            if (i < filled) sb.append("§b■");
            else sb.append("§7□");
        }
        sb.append("§8]");
        return sb.toString();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().title().toString().contains(GUI_TITLE)) {
            event.setCancelled(true);
        }
    }
}
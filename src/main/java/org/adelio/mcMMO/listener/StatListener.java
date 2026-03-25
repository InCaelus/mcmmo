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


        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        for (int i = 0; i < 45; i++) gui.setItem(i, glass);


        gui.setItem(20, createInfoItem(player.getName()));


        gui.setItem(10, createSkillItem(Material.DIAMOND_PICKAXE, "채광", data, SkillType.MINING));
        gui.setItem(16, createSkillItem(Material.DIAMOND_AXE, "벌목", data, SkillType.WOODCUTTING));
        gui.setItem(28, createSkillItem(Material.DIAMOND_SHOVEL, "삽질", data, SkillType.EXCAVATION));
        gui.setItem(34, createSkillItem(Material.DIAMOND_SWORD, "전투", data, SkillType.COMBAT));

        player.openInventory(gui);
    }

    private ItemStack createInfoItem(String name) {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("§b§l" + name + "님의 프로필"));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createSkillItem(Material m, String name, PlayerData data, SkillType type) {
        ItemStack item = new ItemStack(m);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("§e§l" + name + " 스탯"));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("§f레벨: §a" + data.getLevel(type)));
        lore.add(Component.text("§f경험치: §7" + String.format("%.1f", data.getExp(type)) + " / " + (data.getLevel(type) * 100)));
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().title().toString().contains(GUI_TITLE)) event.setCancelled(true);
    }
}
package org.adelio.mcMMO;

import org.adelio.mcMMO.command.AdminCommand;
import org.adelio.mcMMO.command.AdminTabCompleter;
import org.adelio.mcMMO.command.StatsCommand;
import org.adelio.mcMMO.data.DataManager;
import org.adelio.mcMMO.listener.*;
import org.bukkit.plugin.java.JavaPlugin;

public class McMMO extends JavaPlugin {

    private DataManager dataManager;

    @Override
    public void onEnable() {

        dataManager = new DataManager();
        dataManager.init("localhost:3306", "mcmmo", "root", "10234907");

        var pm = getServer().getPluginManager();

        pm.registerEvents(new PlayerJoinQuitListener(dataManager.getPlayerManager()), this);
        pm.registerEvents(new StatListener(dataManager), this);

        pm.registerEvents(new MiningListener(dataManager.getPlayerManager()), this);
        pm.registerEvents(new WoodCuttingListener(dataManager.getPlayerManager()), this);
        pm.registerEvents(new ExcavationListener(dataManager.getPlayerManager()), this);
        pm.registerEvents(new CombatListener(dataManager.getPlayerManager()), this);

        var adminCmd = getCommand("mcmmo");
        if (adminCmd != null) {
            adminCmd.setExecutor(new AdminCommand(dataManager));
            adminCmd.setTabCompleter(new AdminTabCompleter());
        }

        var statsCmd = getCommand("stats");
        if (statsCmd != null) {
            statsCmd.setExecutor(new StatsCommand(dataManager));
        }

        getLogger().info("========================================");
        getLogger().info("               mcMMO                   ");
        getLogger().info("   채광 | 벌목 | 삽질 | 전투 활성화 완료   ");
        getLogger().info("========================================");
    }

    @Override
    public void onDisable() {
        if (dataManager != null) {
            dataManager.save();
            getLogger().info("[mcMMO] 모든 플레이어 데이터를 DB에 안전하게 저장했습니다.");
        }
    }

    public DataManager getDataManager() {
        return dataManager;
    }
}
package com.wagologies.bedwarsPractice;

import com.github.juliarn.npc.NPCPool;
import com.wagologies.bedwarsPractice.Commands.Bedwars;
import com.wagologies.bedwarsPractice.Commands.ToggleSpecialItems;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class BedwarsPractice extends JavaPlugin {
    public static BedwarsPractice instance;
    public WorldCopier worldCopier;
    public Game game;
    public NPCPool npcPool;
    @Override
    public void onEnable() {
        getConfig().addDefault("default", true);
        saveConfig();
        getCommand("bedwars").setExecutor(new Bedwars());
        getCommand("specialItems").setExecutor(new ToggleSpecialItems());
        worldCopier = new WorldCopier(this);
        instance = this;
        npcPool = new NPCPool(this);if (!Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays")) {
            getLogger().severe("*** HolographicDisplays is not installed or not enabled. ***");
            getLogger().severe("*** This plugin will be disabled. ***");
            this.setEnabled(false);
            return;
        }
    }

    @Override
    public void onDisable() {
        instance = null;
        worldCopier = null;
    }
}

package com.kazuha.jsbw;

import com.andrei1058.bedwars.api.BedWars;
import org.bukkit.Bukkit;
import org.bukkit.material.Bed;
import org.bukkit.plugin.java.JavaPlugin;

public class main extends JavaPlugin {
    public static BedWars bedwarsAPI;
    public static JavaPlugin instance;
    @Override
    public void onEnable(){
        instance = this;
        if(Bukkit.getPluginManager().getPlugin("BedWars1058") == null){
            getLogger().warning("Bedwars1058 Not Found. Please make sure it loads before this plugin!");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        Bukkit.getPluginManager().registerEvents(new eventhandler(),this);
        bedwarsAPI = Bukkit.getServicesManager().getRegistration(BedWars .class).getProvider();

    }
}

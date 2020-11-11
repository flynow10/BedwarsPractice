package com.wagologies.bedwarsPractice;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ConfigReader {

    private final File configFile;
    private final FileConfiguration config;
    private final World world;
    private int teamNumber = 0;
    public ConfigReader(File configFile, World world)
    {
        this.configFile = configFile;
        this.world = world;
        if(!configFile.exists())
        {
            BedwarsPractice.instance.getLogger().info("Template file doesn't exist!");
        }
        config = YamlConfiguration.loadConfiguration(configFile);
    }
    public void setTeamNumber(int teamNumber)
    {
        this.teamNumber = teamNumber;
    }
    public int getTeamNumber()
    {
        return teamNumber;
    }
    public Location getSpawnLocation(int teamNumber)
    {
        setTeamNumber(teamNumber);
        return getSpawnLocation();
    }
    public Location getSpawnLocation()
    {
        return new Location(world,
                config.getInt("teams.team" + this.teamNumber + ".spawnLocation.x")+0.5,
                config.getInt("teams.team" + this.teamNumber + ".spawnLocation.y"),
                config.getInt("teams.team" + this.teamNumber + ".spawnLocation.z")+0.5);
    }
    public Location getIronGenerator(int teamNumber)
    {
        setTeamNumber(teamNumber);
        return getIronGenerator();
    }
    public Location getIronGenerator()
    {
        return new Location(world,
                config.getInt("teams.team" + teamNumber + ".ironGen.x")+0.5,
                config.getInt("teams.team" + teamNumber + ".ironGen.y"),
                config.getInt("teams.team" + teamNumber + ".ironGen.z")+0.5);
    }
    public Location getBedHead(int teamNumber)
    {
        setTeamNumber(teamNumber);
        return getBedHead();
    }
    public Location getBedHead()
    {
        return new Location(world,
                config.getInt("teams.team" + teamNumber + ".bedLocation.x"),
                config.getInt("teams.team" + teamNumber + ".bedLocation.y"),
                config.getInt("teams.team" + teamNumber + ".bedLocation.z"));
    }
    public Location getItemVillager(int teamNumber)
    {
        setTeamNumber(teamNumber);
        return getItemVillager();
    }
    public Location getItemVillager()
    {
        return new Location(world,
                config.getInt("teams.team" + teamNumber + ".itemVillager.x")+0.5,
                config.getInt("teams.team" + teamNumber + ".itemVillager.y"),
                config.getInt("teams.team" + teamNumber + ".itemVillager.z")+0.5);
    }
    public Location getTeamVillager()
    {
        return new Location(world,
                config.getInt("teams.team" + teamNumber + ".soloVillager.x")+0.5,
                config.getInt("teams.team" + teamNumber + ".soloVillager.y"),
                config.getInt("teams.team" + teamNumber + ".soloVillager.z")+0.5);
    }
    public List<Location> getEmeraldSpawners()
    {
        List<Location> emeraldSpawners = new ArrayList<>();
        for(String key : config.getConfigurationSection("Emeralds").getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection("Emeralds." + key);
            emeraldSpawners.add(new Location(world, section.getInt("x"), section.getInt("y"), section.getInt("z")));
        }
        return emeraldSpawners;
    }
    public List<Location> getDiamondSpawners()
    {
        List<Location> diamondSpawners = new ArrayList<>();
        for(String key : config.getConfigurationSection("Diamonds").getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection("Diamonds." + key);
            diamondSpawners.add(new Location(world, section.getInt("x"), section.getInt("y"), section.getInt("z")));
        }
        return diamondSpawners;
    }
    public int getMaxTeams()
    {
        return config.getInt("MaxTeams");
    }
}
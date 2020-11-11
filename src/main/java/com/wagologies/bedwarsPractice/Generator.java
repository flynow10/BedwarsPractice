package com.wagologies.bedwarsPractice;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.line.HologramLine;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

public class Generator {
    private final ItemStack itemToSpawn;
    private int secondsBetweenSpawn;
    private final Location spawnPoint;
    private BukkitTask task;
    private Hologram hologram;
    private HologramLine line;
    public int lastSpawn = 0;
    private final Boolean hasHologram;

    public Generator(ItemStack itemToSpawn, int secondsBetweenSpawn, Location spawnPoint, Boolean hasHologram)
    {
        this.itemToSpawn = itemToSpawn;
        this.secondsBetweenSpawn = secondsBetweenSpawn;
        this.spawnPoint = spawnPoint;
        this.hasHologram = hasHologram;
        if(hasHologram) {
            hologram = HologramsAPI.createHologram(BedwarsPractice.instance, spawnPoint.add(0.5,2,0.5));
            hologram.appendTextLine( colorFromMaterial(itemToSpawn.getType()) + "Generator");
            hologram.appendTextLine(ChatColor.GRAY + "Time till next spawn:");
            line = hologram.appendTextLine(colorFromMaterial(itemToSpawn.getType()) + (secondsBetweenSpawn - lastSpawn));
        }
        CountDown();
    }
    public void UpgradeTickSpeed(int newSecondsBetweenSpawn)
    {
        this.secondsBetweenSpawn = newSecondsBetweenSpawn;
    }
    private void CountDown()
    {
        if(lastSpawn >= secondsBetweenSpawn)
        {
            lastSpawn = 0;
            SpawnMaterial();
        }
        else
        {
            lastSpawn++;
            if(hasHologram)
                UpdateHologram();
        }
        task = Bukkit.getScheduler().runTaskLater(BedwarsPractice.instance, () ->{
            CountDown();
        }, 20);
    }
    private void UpdateHologram()
    {
        hologram.removeLine(2);
        line = hologram.appendTextLine(colorFromMaterial(itemToSpawn.getType()) + (secondsBetweenSpawn - lastSpawn));
    }
    private void SpawnMaterial()
    {
        spawnPoint.getWorld().dropItemNaturally(spawnPoint, itemToSpawn);
    }
    public void StopSpawner()
    {
        task.cancel();
        if(hasHologram)
        {
            hologram.delete();
        }
    }
    public String colorFromMaterial(Material material)
    {
        switch (material)
        {
            case EMERALD:
                return ChatColor.GREEN.toString();
            case DIAMOND:
                return ChatColor.AQUA.toString();
            case IRON_INGOT:
                return ChatColor.WHITE.toString();
            case GOLD_INGOT:
                return ChatColor.YELLOW.toString();
            default:
                return "";
        }
    }
}

package com.wagologies.bedwarsPractice.Players;

import com.connorlinfoot.titleapi.TitleAPI;
import com.wagologies.bedwarsPractice.BedwarsPractice;
import com.wagologies.bedwarsPractice.Game;
import com.wagologies.bedwarsPractice.Players.Shop.ShopNPC;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.Array;
import java.util.Arrays;

public class BedwarsPlayer {
    public final BedwarsTeam team;
    public final Player player;
    public final Game game;
    public final BukkitTask checkSwordTask;
    public boolean playerAlive = true, hasSheers = false;
    public int pickaxeLevel = 0, axeLevel = 0;
    public String armorLevel = "LEATHER";
    public BedwarsPlayer attacker = null;

    public BedwarsPlayer(Player player, Game game, BedwarsTeam team)
    {
        this.player = player;
        this.game = game;
        this.team = team;
        player.teleport(team.spawnLocation);
        player.getEnderChest().clear();
        player.setGameMode(GameMode.SURVIVAL);
        player.setHealth(20);
        player.setFoodLevel(20);
        player.getInventory().clear();
        GiveRespawnItems();
        checkSwordTask = Bukkit.getScheduler().runTaskTimer(BedwarsPractice.instance, () -> CheckIfGiveSword(), 0L, 100L);
    }
    public void GiveArmor()
    {
        ItemStack[] armor = new ItemStack[4];
        armor[2] = new ItemStack(Material.LEATHER_CHESTPLATE);
        armor[3] = new ItemStack(Material.LEATHER_HELMET);
        switch (armorLevel)
        {
            case "LEATHER":
                armor[0] = new ItemStack(Material.LEATHER_BOOTS);
                armor[1] = new ItemStack(Material.LEATHER_LEGGINGS);
                break;
            case "CHAIN":
                armor[0] = new ItemStack(Material.CHAINMAIL_BOOTS);
                armor[1] = new ItemStack(Material.CHAINMAIL_LEGGINGS);
                break;
            case "IRON":
                armor[0] = new ItemStack(Material.IRON_BOOTS);
                armor[1] = new ItemStack(Material.IRON_LEGGINGS);
                break;
            case "DIAMOND":
                armor[0] = new ItemStack(Material.DIAMOND_BOOTS);
                armor[1] = new ItemStack(Material.DIAMOND_LEGGINGS);
                break;
        }
        for(ItemStack item : armor)
        {
            ItemMeta enchantment = item.getItemMeta();
            enchantment.spigot().setUnbreakable(true);
            enchantment.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
            if(team.protectionLevel != 0)
                enchantment.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, team.protectionLevel, false);
            item.setItemMeta(enchantment);
        }
        player.getInventory().setArmorContents(armor);
    }
    public void AddSharpness()
    {
        for(int i = 0; i < player.getInventory().getContents().length; i++)
        {
            ItemStack item = player.getInventory().getItem(i);
            if(item == null || item.getType() == null)
                continue;
            if(Arrays.stream(game.swords).anyMatch(item.getType()::equals))
            {
                item.addEnchantment(Enchantment.DAMAGE_ALL, 1);
            }
        }
    }
    public void AddProtection()
    {
        for (int i = 0; i < player.getInventory().getArmorContents().length; i++) {
            ItemStack item = player.getInventory().getArmorContents()[i];
            if(item == null || item.getType() == null)
                continue;
            item.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL,team.protectionLevel);
        }
    }

    public void OnDeath()
    {
        if(!team.bedAlive)
        {
            TitleAPI.sendTitle(player, 10, 50, 10, ChatColor.RED + "Game Over", "Maybe you'll win next time!");
            team.playerCount--;
            game.VictoryCheck();
        }
        else {
            TitleAPI.sendTitle(player, 10, 50, 10, ChatColor.RED + "YOU DIED", "Respawning in 5 seconds");
            BukkitScheduler scheduler = Bukkit.getScheduler();
            scheduler.scheduleSyncDelayedTask(BedwarsPractice.instance, () -> Respawn(), 100L);
        }
        if(attacker != null)
        {
            int iron = 0, gold = 0, diamond = 0, emerald = 0;
            for(ItemStack item : player.getInventory().getContents())
            {
                if(item == null || item.getType() == null)
                    continue;
                switch (item.getType())
                {
                    case IRON_INGOT:
                        iron += item.getAmount();
                        break;
                    case GOLD_INGOT:
                        gold += item.getAmount();
                        break;
                    case DIAMOND:
                        diamond += item.getAmount();
                        break;
                    case EMERALD:
                        emerald += item.getAmount();
                        break;
                }
            }
            if(iron > 0 || gold > 0 || diamond > 0 || emerald > 0)
            {
                attacker.player.sendMessage(ChatColor.GREEN + "You gained some resources from " + player.getDisplayName() + "'s inventory:");
                attacker.player.playSound(attacker.player.getLocation(),Sound.ORB_PICKUP, 1, 1);
            }
            if(iron > 0)
            {
                attacker.player.getInventory().addItem(new ItemStack(Material.IRON_INGOT, iron));
                attacker.player.sendMessage(ChatColor.WHITE + "+" + iron + " Iron");
            }
            if(gold > 0)
            {
                attacker.player.getInventory().addItem(new ItemStack(Material.GOLD_INGOT, gold));
                attacker.player.sendMessage(ChatColor.YELLOW + "+" + gold + " Gold");
            }
            if(diamond > 0)
            {
                attacker.player.getInventory().addItem(new ItemStack(Material.DIAMOND, diamond));
                attacker.player.sendMessage(ChatColor.AQUA + "+" + diamond + " Diamond");
            }
            if(emerald > 0)
            {
                attacker.player.getInventory().addItem(new ItemStack(Material.EMERALD, emerald));
                attacker.player.sendMessage(ChatColor.GREEN + "+" + emerald + " Emerald");
            }
        }
        attacker = null;
        player.getInventory().clear();
        player.setGameMode(GameMode.SPECTATOR);
        player.setHealth(20);
        player.teleport(game.deathLocation);
        playerAlive = false;
    }
    public void OnDisconnect()
    {
        if(team.playerCount == 1)
        {
            team.DestroyBed();
        }
        team.playerCount--;
        game.VictoryCheck();
    }
    public void OnVictory()
    {
        player.getInventory().clear();
        player.setHealth(20);
        TitleAPI.sendTitle(player, 10,40,10,ChatColor.YELLOW + "Victory", "You win!");
    }
    public void OnGameEnd()
    {
        Bukkit.getScheduler().cancelTask(checkSwordTask.getTaskId());
    }
    public void Respawn()
    {
        player.setGameMode(GameMode.SURVIVAL);
        player.teleport(team.spawnLocation);
        player.getInventory().clear();
        GiveRespawnItems();
        playerAlive = true;

    }
    public void GiveRespawnItems()
    {
        GiveArmor();
        GiveSword(Material.WOOD_SWORD);
        GiveShears();
    }
    public void GiveSword(Material sword)
    {
        ItemStack swordItem = new ItemStack(sword, 1);
        ItemMeta swordMeta = swordItem.getItemMeta();
        if(team.hasSharpness)
            swordMeta.addEnchant(Enchantment.DAMAGE_ALL, 1, false);
        swordMeta.spigot().setUnbreakable(true);
        swordMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        swordItem.setItemMeta(swordMeta);
        if(player.getInventory().getItem(0) == null || player.getInventory().getItem(0).getType() == Material.WOOD_SWORD) {
            player.getInventory().setItem(0, swordItem);
            return;
        }
        player.getInventory().addItem(swordItem);
    }
    public void GiveShears()
    {
        if(hasSheers)
        {
            ItemStack shears = new ItemStack(Material.SHEARS);
            ItemMeta shearsMeta = shears.getItemMeta();
            shearsMeta.spigot().setUnbreakable(true);
            shearsMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
            shears.setItemMeta(shearsMeta);
            player.getInventory().addItem(shears);
        }
    }
    public void CheckIfGiveSword() {
        if(playerAlive) {
            boolean hasSword = false;
            ItemStack[] inventory = player.getInventory().getContents();
            for (int i = 0; i < inventory.length; i++) {
                ItemStack item = inventory[i];
                if (item == null)
                    continue;
                if (Arrays.stream(game.swords).anyMatch(item.getType()::equals)) {
                    if (hasSword) {
                        if(item.getType() == Material.WOOD_SWORD)
                        {
                            player.getInventory().setItem(i, new ItemStack(Material.AIR));
                        }
                    }
                    hasSword = true;
                }
            }
            if (!hasSword)
                GiveSword(Material.WOOD_SWORD);
        }
    }
}

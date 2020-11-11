package com.wagologies.bedwarsPractice.Players;

import com.connorlinfoot.titleapi.TitleAPI;
import com.wagologies.bedwarsPractice.BedwarsPractice;
import com.wagologies.bedwarsPractice.Game;
import com.wagologies.bedwarsPractice.Generator;
import com.wagologies.bedwarsPractice.Players.Shop.ShopNPC;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class BedwarsTeam {
    public int playerCount;
    public final String displayName;
    public final ChatColor color;
    private final Game game;
    public final Location spawnLocation;
    private final Location generator;
    public final Location bedLocation;
    public Generator ironGen;
    public Generator goldGen;
    public int protectionLevel = 0;
    public boolean hasSharpness = false;
    public boolean bedAlive = true;
    public List<BedwarsPlayer> players = new ArrayList<>();
    public ShopNPC items;
    public ShopNPC upgrades;

    public BedwarsTeam(int playerCount, String displayName, ChatColor color, Game game)
    {
        this.playerCount = playerCount;
        this.displayName = displayName;
        this.color = color;
        this.game = game;
        this.spawnLocation = game.configReader.getSpawnLocation();
        this.generator = game.configReader.getIronGenerator();
        this.bedLocation = game.configReader.getBedHead();
        SpawnShops();
        StartSpawner();
    }
    public void AddSharpness()
    {
        hasSharpness = true;
        for(BedwarsPlayer player : players)
        {
            player.AddSharpness();
        }
    }
    public void AddProtection() {
        protectionLevel++;
        for(BedwarsPlayer player : players)
        {
            player.AddProtection();
        }
    }
    public void SpawnShops()
    {
        items = new ShopNPC(game, game.configReader.getItemVillager(), spawnLocation, ShopNPC.ShopType.ITEMS);
        upgrades = new ShopNPC(game, game.configReader.getTeamVillager(), spawnLocation, ShopNPC.ShopType.UPGRADES);
    }
    private void StartSpawner()
    {
        ironGen = new Generator(new ItemStack(Material.IRON_INGOT), 1,generator,false);
        goldGen = new Generator(new ItemStack(Material.GOLD_INGOT), 4,generator,false);
    }
    public void AddPlayer(Player player)
    {
        players.add(new BedwarsPlayer(player, game, this));
    }
    public void DestroyBed()
    {
        bedAlive = false;
        game.world.getBlockAt(bedLocation).breakNaturally();
        game.world.strikeLightningEffect(bedLocation);
        for(BedwarsPlayer bedwarsPlayer : players)
        {
            TitleAPI.sendTitle(bedwarsPlayer.player, 10,30,10,ChatColor.RED + "BED DESTROYED", "You will no longer respawn!");
        }
        for(Player player : game.GetAllPlayers())
        {
            player.sendMessage("-------------------------------------------");
            player.sendMessage("              Bed Destruction!             ");
            player.sendMessage("        " + color + displayName + ChatColor.WHITE + "'s bed was destroyed!        ");
            player.sendMessage("-------------------------------------------");
        }
    }
    public void OnEndGame()
    {
        items.Endgame();
        items = null;
        upgrades.Endgame();
        upgrades = null;
        ironGen.StopSpawner();
        goldGen.StopSpawner();
    }
}

package com.wagologies.bedwarsPractice;

import com.wagologies.HubPlugin.HubPlugin;
import com.wagologies.bedwarsPractice.Players.BedwarsPlayer;
import com.wagologies.bedwarsPractice.Players.BedwarsTeam;
import com.wagologies.bedwarsPractice.Players.SpecialItems;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Game implements Listener {
    public List<BedwarsTeam> teams = new ArrayList<>();
    public Material[] breakables = new Material[] {
            Material.WOOL,
            Material.WOOD,
            Material.ENDER_STONE,
            Material.GLASS,
            Material.OBSIDIAN,
            Material.BED_BLOCK
    };
    public Material[] swords = new Material[] {
            Material.WOOD_SWORD,
            Material.STONE_SWORD,
            Material.IRON_SWORD,
            Material.DIAMOND_SWORD
    };
    public boolean gameRunning;
    public World world;
    public ConfigReader configReader;
    public Location deathLocation;
    public List<Generator> emeraldGenerators = new ArrayList<>();
    public List<Generator> diamondGenerators = new ArrayList<>();

    public Game(List<List<Player>> teams, World world)
    {
        gameRunning = true;
        this.world = world;
        deathLocation = new Location(world, 0.5,120, 0.5);
        configReader = new ConfigReader(new File(BedwarsPractice.instance.getDataFolder(), File.separator + "TemplateWorlds" + File.separator + "Bedwars Template 2.yml"), world);
        Bukkit.getPluginManager().registerEvents(this, BedwarsPractice.instance);
        SetWorldDefaults();
        StartGenerators();
        AddPlayers(teams);
        LoadChunks();
        new SpecialItems();
    }
    public BedwarsPlayer GetBedwarsPlayerFromPlayer(Player player)
    {
        for(BedwarsTeam team : teams)
        {
            for(BedwarsPlayer bedwarsPlayer : team.players)
            {
                if(bedwarsPlayer.player.equals(player))
                    return bedwarsPlayer;
            }
        }
        return null;
    }
    public List<Player> GetAllPlayers()
    {
        List<Player> players = new ArrayList<>();
        for(BedwarsTeam team : teams)
        {
            for(BedwarsPlayer bedwarsPlayer : team.players)
            {
                players.add(bedwarsPlayer.player);
            }
        }
        return players;
    }
    public List<BedwarsTeam> GetTeamsAlive()
    {
        List<BedwarsTeam> bedwarsTeams = new ArrayList<>();
        for(BedwarsTeam bedwarsTeam : teams)
        {
            if(bedwarsTeam.playerCount > 0)
                bedwarsTeams.add(bedwarsTeam);
        }
        return bedwarsTeams;
    }
    public void VictoryCheck()
    {
        List<BedwarsTeam> teamsAlive = GetTeamsAlive();
        if(teamsAlive.size() == 1)
            Victory(teamsAlive.get(0));
    }
    public void Victory(BedwarsTeam winner)
    {
        for(BedwarsPlayer player : winner.players)
        {
            player.OnVictory();
        }
        for(BedwarsTeam team : teams)
        {
            team.OnEndGame();
        }
        for (Generator emeraldGenerator : emeraldGenerators) {
            emeraldGenerator.StopSpawner();
        }
        for (Generator diamondGenerator : diamondGenerators) {
            diamondGenerator.StopSpawner();
        }
        gameRunning = false;
        Bukkit.getScheduler().scheduleSyncDelayedTask(BedwarsPractice.instance, this::EndGame, 100L);
        SpecialItems.Stop();
    }
    public void EndGame()
    {
        for(Player player : GetAllPlayers())
        {
            GetBedwarsPlayerFromPlayer(player).OnGameEnd();
        }
        HandlerList.unregisterAll(this);
        GetAllPlayers().forEach(player -> {
            HubPlugin.TeleportToHub(player);
        });
        Bukkit.unloadWorld("bedwars", false);
        BedwarsPractice.instance.game = null;
    }
    private void SetWorldDefaults()
    {
        world.getWorldBorder().setCenter(new Location(world, 0, 0, 0));
        world.getWorldBorder().setSize(150);
        world.setDifficulty(Difficulty.NORMAL);
        world.setTime(1000);
        world.setGameRuleValue("doDaylightCycle", "false");
        world.setWeatherDuration(0);
    }
    private void StartGenerators()
    {
        for(Location diamondGen : configReader.getDiamondSpawners())
        {
            diamondGenerators.add(new Generator(new ItemStack(Material.DIAMOND), 20, diamondGen, true));
        }
        for(Location emeraldGen : configReader.getEmeraldSpawners())
        {
            emeraldGenerators.add(new Generator(new ItemStack(Material.EMERALD), 40, emeraldGen, true));
        }
    }
    private void AddPlayers(List<List<Player>> teams)
    {
        if(teams.size() < configReader.getMaxTeams())
        {
            while(teams.size() < configReader.getMaxTeams())
            {
                teams.add(new ArrayList<>());
            }
        }
        Collections.shuffle(teams);
        for(List<Player> players : teams)
        {
            if(players.size() == 0)
            {
                configReader.setTeamNumber(configReader.getTeamNumber()+1);
                continue;
            }
            int id = this.teams.size();
            BedwarsTeam team = new BedwarsTeam(players.size(),  "Team " + id, ChatColor.AQUA, this);
            this.teams.add(team);
            for(Player player : players)
            {
                team.AddPlayer(player);
            }
            configReader.setTeamNumber(configReader.getTeamNumber()+1);
        }
    }
    private void LoadChunks()
    {
        for(int i = -128; i < 128; i+=16)
        {
            for(int j = -128; i < 128; i+=16) {
                world.loadChunk(i, j, false);
            }
        }
    }

    @EventHandler
    public void BreakBlock(BlockBreakEvent event)
    {
        if(gameRunning) {
            BedwarsPlayer bedwarsPlayer = GetBedwarsPlayerFromPlayer(event.getPlayer());
            if (bedwarsPlayer != null) {
                if (!Arrays.stream(breakables).anyMatch(event.getBlock().getType()::equals)) {
                    if (event.isCancelled() != true) {
                        event.setCancelled(true);
                    }
                }
                if (event.getBlock().getType() == Material.BED_BLOCK) {
                    Location l = getBed(event.getBlock().getLocation());
                    Location block1 = new Location(world, event.getBlock().getX(), event.getBlock().getY(), event.getBlock().getZ());
                    Location block2 = new Location(world, l.getBlockX(), l.getBlockY(), l.getBlockZ());
                    for (BedwarsTeam team : teams) {
                        BedwarsPractice.instance.getLogger().info(team.displayName);
                        BedwarsPractice.instance.getLogger().info(team.bedLocation.toString());
                        BedwarsPractice.instance.getLogger().info(block1.toString());
                        BedwarsPractice.instance.getLogger().info(block2.toString());
                        boolean bedMatch = block1.equals(team.bedLocation) || block2.equals(team.bedLocation);
                        BedwarsPractice.instance.getLogger().info(String.valueOf(bedMatch));
                        if (bedMatch) {
                            if (!event.isCancelled()) event.setCancelled(true);
                            for(BedwarsPlayer player : team.players)
                            {
                                if(bedwarsPlayer.player == player.player) {
                                    player.player.sendMessage(ChatColor.RED + "You can't break your own bed silly!");
                                    return;
                                }
                            }
                            l.getBlock().setType(Material.AIR);
                            event.getBlock().setType(Material.AIR);
                            team.DestroyBed();
                            return;
                        }
                    }
                }
            }
        }
    }
    public static Location getBed(Location l) {
        return checkForMaterial(l, Material.BED_BLOCK);
    }
    private static Location checkForMaterial(Location l, Material mat) {
        Location returnV = null;
        List<Location> locs = new ArrayList<Location>();
        locs.add(new Location(l.getWorld(), l.getX() + 1, l.getY(), l.getZ()));
        locs.add(new Location(l.getWorld(), l.getX(), l.getY(), l.getZ() + 1));
        locs.add(new Location(l.getWorld(), l.getX() + 1, l.getY(), l.getZ() + 1));
        locs.add(new Location(l.getWorld(), l.getX() - 1, l.getY(), l.getZ()));
        locs.add(new Location(l.getWorld(), l.getX(), l.getY(), l.getZ() - 1));
        locs.add(new Location(l.getWorld(), l.getX() - 1, l.getY(), l.getZ() -1));
        locs.add(new Location(l.getWorld(), l.getX() + 1, l.getY(), l.getZ() - 1));
        locs.add(new Location(l.getWorld(), l.getX() - 1, l.getY(), l.getZ() + 1));
        locs.add(new Location(l.getWorld(), l.getX() + 1, l.getY() + 1, l.getZ()));
        locs.add(new Location(l.getWorld(), l.getX(), l.getY() + 1, l.getZ() + 1));
        locs.add(new Location(l.getWorld(), l.getX() + 1, l.getY() + 1, l.getZ() + 1));
        locs.add(new Location(l.getWorld(), l.getX() - 1, l.getY() + 1, l.getZ()));
        locs.add(new Location(l.getWorld(), l.getX(), l.getY() + 1, l.getZ() - 1));
        locs.add(new Location(l.getWorld(), l.getX() - 1, l.getY() + 1, l.getZ() -1));
        locs.add(new Location(l.getWorld(), l.getX() + 1, l.getY() + 1, l.getZ() - 1));
        locs.add(new Location(l.getWorld(), l.getX() - 1, l.getY() + 1, l.getZ() + 1));
        locs.add(new Location(l.getWorld(), l.getX() + 1, l.getY() - 1, l.getZ()));
        locs.add(new Location(l.getWorld(), l.getX(), l.getY() - 1, l.getZ() + 1));
        locs.add(new Location(l.getWorld(), l.getX() + 1, l.getY() - 1, l.getZ() + 1));
        locs.add(new Location(l.getWorld(), l.getX() - 1, l.getY() - 1, l.getZ()));
        locs.add(new Location(l.getWorld(), l.getX(), l.getY() - 1, l.getZ() - 1));
        locs.add(new Location(l.getWorld(), l.getX() - 1, l.getY() - 1, l.getZ() -1));
        locs.add(new Location(l.getWorld(), l.getX() + 1, l.getY() - 1, l.getZ() - 1));
        locs.add(new Location(l.getWorld(), l.getX() - 1, l.getY() - 1, l.getZ() + 1));
        for(Location ll : locs) {
            Block b = ll.getBlock();
            if(b.getType() == mat) {
                returnV = ll;
                break;
            }
        }
        return returnV;
    }
    @EventHandler
    public void FoodDepletion(FoodLevelChangeEvent event)
    {
        if(event.getEntity() instanceof Player)
        {
            Player eventPlayer = (Player) event.getEntity();
            BedwarsPlayer bedwarsPlayer = GetBedwarsPlayerFromPlayer(eventPlayer);
            if(bedwarsPlayer != null)
            {
                if(event.isCancelled() != true)
                {
                    event.setCancelled(true);
                }
            }
        }
    }
    @EventHandler(priority = EventPriority.NORMAL)
    public void preventArmorRemoving(InventoryClickEvent event)
    {
        if(event.getSlotType() == InventoryType.SlotType.ARMOR)
        {
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void DeathCheck(EntityDamageEvent event)
    {
        if(event.getEntity() instanceof Player)
        {
            Player eventPlayer = (Player) event.getEntity();
            BedwarsPlayer bedwarsPlayer = GetBedwarsPlayerFromPlayer(eventPlayer);
            if(bedwarsPlayer != null) {
                if (event.getCause() == EntityDamageEvent.DamageCause.VOID || eventPlayer.getHealth() - event.getDamage() <= 0) {
                    if(!event.isCancelled())
                        event.setCancelled(true);
                    SendDeathMessage(bedwarsPlayer, event.getCause());
                    bedwarsPlayer.OnDeath();
                }
            }
        }
    }
    public void SendDeathMessage(BedwarsPlayer player, EntityDamageEvent.DamageCause cause)
    {
        String message;
        String playerName = ChatColor.YELLOW + player.player.getDisplayName();
        Boolean hasAttacker = player.attacker != null;
        String attackerName = "";
        if(hasAttacker)
            attackerName = ChatColor.YELLOW + player.attacker.player.getDisplayName();
        switch (cause)
        {
            case CONTACT:
                message = playerName + ChatColor.RED + " was pricked to death by cactus.";
                break;
            case ENTITY_ATTACK:
                message = playerName + ChatColor.RED + " was murdered by " + attackerName + ChatColor.RED + ".";
                break;
            case PROJECTILE:
                if(hasAttacker)
                    message = playerName + ChatColor.RED + " was sniped by " + attackerName + ChatColor.RED + " from " + ChatColor.BOLD + ChatColor.YELLOW + (int)player.player.getLocation().distance(player.attacker.player.getLocation()) + "m" + ChatColor.RESET + ChatColor.RED + ".";
                else
                    message = playerName + ChatColor.RED + " was shot but by who?";
                break;
            case FALL:
                if(hasAttacker)
                    message = playerName + ChatColor.RED + " was pushed off a cliff by " + attackerName + ChatColor.RED + ".";
                else
                    message = playerName + ChatColor.RED + " jumped to their death.";
                break;
            case FIRE:
            case FIRE_TICK:
                if(hasAttacker)
                    message = playerName + ChatColor.RED + " was burned alive by " + attackerName + ChatColor.RED + ".";
                else
                    message = playerName + ChatColor.RED + " thought they could walk through fire.";
                break;
            case LAVA:
                message = playerName + ChatColor.RED + " managed to find lava in the sky?!";
                break;
            case DROWNING:
                message = playerName + ChatColor.RED + " couldn't find the surface.";
                break;
            case BLOCK_EXPLOSION:
            case ENTITY_EXPLOSION:
                message = playerName + ChatColor.RED + " blew up.";
                break;
            case VOID:
                if(hasAttacker)
                    message = playerName + ChatColor.RED + " was knocked in to the void by " + attackerName + ChatColor.RED + ".";
                else
                {
                    message = playerName + ChatColor.RED + "fell into the void.";
                }
                break;

            case LIGHTNING:
                message = playerName + ChatColor.RED + " has been smited by the " + ChatColor.BOLD +  "POWER OF ZEUS" + ChatColor.RESET + ChatColor.RED + ".";
                break;
            case SUICIDE:
                message = playerName + ChatColor.RED + " has unfortunately taken their own life.";
                break;
            case POISON:
                message = ChatColor.RED + "Someone slipped poison in " + playerName + "'s" + ChatColor.RED + " morning coffee.";
                break;
            case MAGIC:
                message = playerName + ChatColor.RED + " was killed by unspeakable dark arts.";
                break;
            case WITHER:
                message = playerName + ChatColor.RED + " withered away.";
                break;
            case FALLING_BLOCK:
                message = playerName + ChatColor.RED + " has been crushed by a falling block.";
                break;
            default:
                message = ChatColor.YELLOW + playerName + " died.";
        }
        if(!player.team.bedAlive)
            message+= ChatColor.AQUA + " FINAL KILL";
        String finalMessage = message;
        teams.forEach(bedwarsTeam -> bedwarsTeam.players.forEach(bedwarsPlayer -> bedwarsPlayer.player.sendMessage(finalMessage)));
    }
    @EventHandler (priority = EventPriority.HIGHEST)
    public void AttackHandler(EntityDamageByEntityEvent event)
    {
        if(event.getDamager() instanceof Player)
        {
            Player attackingPlayer = (Player) event.getDamager();
            BedwarsPlayer attackingBedwarsPlayer = GetBedwarsPlayerFromPlayer(attackingPlayer);
            if(attackingBedwarsPlayer != null)
            {
                if(event.getEntity() instanceof Player) {
                    Player attackedPlayer = (Player) event.getEntity();
                    BedwarsPlayer attackedBedwarsPlayer = GetBedwarsPlayerFromPlayer(attackedPlayer);

                    if(attackedBedwarsPlayer != null)
                    {
                        if(attackedBedwarsPlayer.team.players.contains(attackingBedwarsPlayer))
                        {
                            if(!event.isCancelled())
                                event.setCancelled(true);
                            return;
                        }
                        attackedBedwarsPlayer.attacker = attackingBedwarsPlayer;
                    }
                }
            }
        }
    }
    @EventHandler
    public void DisconnectHandler(PlayerQuitEvent event)
    {
        BedwarsPlayer bedwarsPlayer = GetBedwarsPlayerFromPlayer(event.getPlayer());
        if(bedwarsPlayer != null)
        {
            bedwarsPlayer.OnDisconnect();
        }
    }
    @EventHandler
    public void WeatherChange(WeatherChangeEvent event)
    {
        if(!event.isCancelled())
            event.setCancelled(true);
    }
    @EventHandler
    public void GrassDeath(BlockSpreadEvent event)
    {
        if(!event.isCancelled())
            event.setCancelled(true);
    }
}

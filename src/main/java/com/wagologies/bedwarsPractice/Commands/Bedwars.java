package com.wagologies.bedwarsPractice.Commands;

import com.wagologies.bedwarsPractice.BedwarsPractice;
import com.wagologies.bedwarsPractice.Game;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.nio.channels.Channel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Bedwars implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(commandSender instanceof Player)
        {
            Player player = (Player)commandSender;
            if(Bukkit.getWorld("bedwars") == null) {
                if(strings.length == 0)
                {
                    World world = BedwarsPractice.instance.worldCopier.copyTemplateWorld("bedwars");
                    List<List<Player>> soloTeam = new ArrayList<>();
                    soloTeam.add(new ArrayList<>());
                    soloTeam.get(0).add(player);
                    BedwarsPractice.instance.game = new Game(soloTeam, world);
                    return true;
                }
                Player secondPlayer = Bukkit.getPlayer(strings[0]);
                if (secondPlayer != null) {
                    World world = BedwarsPractice.instance.worldCopier.copyTemplateWorld("bedwars");
                    if(world != null) {
                        secondPlayer.sendMessage(ChatColor.GREEN + player.getDisplayName() + " has started a game of bedwars with you");
                        List<List<Player>> teams = new ArrayList<>();
                        teams.add(new ArrayList<>());
                        teams.get(0).add(player);
                        teams.add(new ArrayList<>());
                        teams.get(1).add(secondPlayer);
                        BedwarsPractice.instance.game = new Game(teams, world);
                        return true;
                    }
                    player.sendMessage(ChatColor.RED + "Please check server log, something went wrong");
                    return true;
                }
                player.sendMessage(ChatColor.RED + "Player " + ChatColor.AQUA + strings[0] + ChatColor.RED + " could not be found");
                return true;
            }
            player.sendMessage(ChatColor.RED + "A game is already in progress please wait");
            return true;
        }
        commandSender.sendMessage(ChatColor.RED + "This command can only be run as a player");
        return true;
    }
}

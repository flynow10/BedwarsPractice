package com.wagologies.bedwarsPractice.Commands;

import com.wagologies.bedwarsPractice.Players.SpecialItems;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ToggleSpecialItems implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(strings.length != 1)
            return false;
        switch (strings[0])
        {
            case "on":
                new SpecialItems();
                return true;
            case "off":
                SpecialItems.Stop();
                return true;
            default:
                return false;
        }
    }
}

package com.wagologies.bedwarsPractice.Players.Shop;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class ShopItem {
    private final Function<Player, Boolean> purchaseCallback;
    Supplier<String> name;
    Supplier<String> description;
    ItemStack placeHolder;
    Material currency;
    Supplier<Integer> cost;
    public ShopItem(Supplier<String> name, Supplier<String> description, Material placeHolderMaterial, int placeHolderStackSize, Material currency, Supplier<Integer> cost, Function<Player,Boolean> purchaseCallback)
    {
        placeHolder = new ItemStack(placeHolderMaterial, placeHolderStackSize);
        this.currency = currency;
        this.name = name;
        this.description = description;
        this.cost = cost;
        this.purchaseCallback = purchaseCallback;
    }
    public ItemStack getItemStack()
    {
        ItemMeta placeHolderMeta = placeHolder.getItemMeta();
        placeHolderMeta.setDisplayName(name.get());
        List<String> descriptionLines = new ArrayList<>();
        descriptionLines.addAll(Arrays.asList(description.get().split("\n")));
        descriptionLines.add("");
        String currencyDisplayname = "";
        String currencyChatColor = "";
        switch (currency)
        {
            case IRON_INGOT:
                currencyDisplayname = "Iron Ingot" + ((cost.get() > 1) ? "s" : "");
                currencyChatColor = String.valueOf(ChatColor.GRAY);
                break;
            case GOLD_INGOT:
                currencyDisplayname = "Gold Ingot" + ((cost.get() > 1) ? "s" : "");
                currencyChatColor = String.valueOf(ChatColor.YELLOW);
                break;
            case DIAMOND:
                currencyDisplayname = "Diamond" + ((cost.get() > 1) ? "s" : "");
                currencyChatColor = String.valueOf(ChatColor.AQUA);
                break;
            case EMERALD:
                currencyDisplayname = "Emerald" + ((cost.get() > 1) ? "s" : "");
                currencyChatColor = String.valueOf(ChatColor.GREEN);
                break;
        }
        if(!(cost.get() == Integer.MAX_VALUE))
            descriptionLines.add(currencyChatColor+ "Costs " + cost.get() + " " + currencyDisplayname);
        else
            descriptionLines.add(ChatColor.RED + "MAX LEVEL");
        placeHolderMeta.setLore(descriptionLines);
        placeHolder.setItemMeta(placeHolderMeta);
        return placeHolder;
    }
    public void PurchaseItem(Player player)
    {
        if(player.getInventory().contains(currency,cost.get())) {
            if(currency == null || player.getInventory() == null)
                return;
            int finalCost = cost.get();
            if(purchaseCallback.apply(player))
            {
                if (finalCost == Integer.MAX_VALUE) {
                    player.getInventory().remove(currency);
                    return;
                }
                player.getInventory().removeItem(new ItemStack(currency,finalCost));
            }
            player.playSound(player.getLocation(), Sound.ORB_PICKUP,1,1);
            player.sendMessage(ChatColor.GREEN + "You purchased: " + placeHolder.getItemMeta().getDisplayName());

        }
        else
            player.sendMessage(ChatColor.RED + "Hey! You don't have enough resources to purchase this!");
    }
}

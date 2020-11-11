package com.wagologies.bedwarsPractice.Players.Shop;

import com.wagologies.bedwarsPractice.BedwarsPractice;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class Shop implements InventoryHolder, Listener
{
    public final Inventory inventory;
    public HashMap<Integer, ShopItem> shopItems = new HashMap<>();

    public Shop(ShopNPC.ShopType type)
    {
        inventory = Bukkit.createInventory(this,54, type.toString());
        Bukkit.getPluginManager().registerEvents(this, BedwarsPractice.instance);
    }
    public void addItemToShop(int slot, ShopItem item)
    {
        shopItems.put(slot, item);
        ResetItems();
    }
    private void ResetItems()
    {
        getInventory().clear();
        for(int i = 0; i < getInventory().getSize(); i++)
        {
            getInventory().setItem(i,new ItemStack(Material.STAINED_GLASS_PANE));
        }
        shopItems.forEach((slot, item) -> {
            getInventory().setItem(slot,item.getItemStack());
        });
        for (HumanEntity viewer : getInventory().getViewers()) {
            if(viewer instanceof Player)
                ((Player) viewer).updateInventory();
        }
    }
    /**
     * Get the object's inventory.
     *
     * @return The inventory.
     */
    @Override
    public Inventory getInventory() {
        return inventory;
    }
    @EventHandler
    public void InventoryClick(InventoryClickEvent event)
    {
        if(event.getInventory().getHolder() instanceof Shop)
        {
            if(event.getInventory().getHolder() == this)
            {
                if(!event.isCancelled())
                    event.setCancelled(true);
                if(event.getRawSlot() == -999)
                    return;
                if(event.getRawSlot() >= 54)
                    return;
                if(shopItems.containsKey(event.getSlot())) {
                    shopItems.get(event.getSlot()).PurchaseItem((Player) event.getWhoClicked());
                    ResetItems();
                }
            }
        }
    }
}
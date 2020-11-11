package com.wagologies.bedwarsPractice.Players.Shop;

import com.github.juliarn.npc.NPC;
import com.github.juliarn.npc.event.PlayerNPCInteractEvent;
import com.github.juliarn.npc.profile.Profile;
import com.wagologies.bedwarsPractice.BedwarsPractice;
import com.wagologies.bedwarsPractice.Game;
import com.wagologies.bedwarsPractice.Players.BedwarsTeam;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.InventoryManager;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

public class ShopNPC implements Listener {
    public NPC npc;
    public Game game;
    public ShopType type;
    public ShopNPC(Game game, Location spawnLocation, Location lookAtPoint, ShopType type)
    {
        this.game = game;
        this.type = type;
        Profile profile = new Profile(type.toString());
        profile.setUniqueId(UUID.randomUUID());
        Collection<Profile.Property> properties = new ArrayList<>();
        properties.add(new Profile.Property("textures",
                "ewogICJ0aW1lc3RhbXAiIDogMTYwNDkyODQ1NzQyMywKICAicHJvZmlsZUlkIiA6ICJmY2UzZTAzMWRkMWI0MTQ2OGIwYWRhZTcxYmQ2NDI3NiIsCiAgInByb2ZpbGVOYW1lIiA6ICJSb2NrU2F3U3RhciIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS82MmQxNzFlYWQ4NDE3M2VjMGJlOWMxYTQzOTRjMzg5NjY3MzczZjUzN2UyMWUzYTcwZDJmZDYyMzliYTU4Y2RkIgogICAgfQogIH0KfQ==",
                "RkvjidbeoLkBy4PGcnDD/bRGEKFsaive3/pfwWIKz9Hbw4jIQzV/PdJ+kYi6FPrgpGwSndZbHmismqFVEgKrOQPbLxFimx4/xRHWeWYMQb/oWiNkyQlJW5RR9jHcsM4fediQBCXJVw3pG6rv3KNGezPeKQDoruqMsXXiI1RamBNmcqK4eHB6ADGp6igjDdsGvpOaQzDp3hEkHRbmiu5vnFApm5CqtRvE9HbZ2vYiI7qncQOilKd2SzPF0gwWls31ZC9K2G1MUYukdRa1IdFYd5tAKEU/k1wdkBx57LYtyGQYQjCZCiWc6K2QP4oKudylBhGzll8WVeiC7WkOF5I+NHpW4wpaxESpmJ1h2ZKogyibzh3lCtj6OhuuaAM2CGLyB1JNpLk1G7va3NkjoGIgGjN1hFDmg8qY55OhAHGZRVSHO2q+HuU3MM6vFYuOFKIjkilQZ7aK32PtYE61BjcUPp9ulwfqVWanQwm0lqT6blrbwXTpAUL3nvO/GUfjYjeoVQdJv5P9WheM3W9CPZNC/v/tO1YmIzujgA503BZejtajgJ+8OS4oWxA5xuYE9Q3QFftA7r4Gb22mQ8OfjwXRdsn+ag/T6oud31+t9OyparEzSbRX/emm0DXwboBcDgPX7ULQ5OX0coFdWEjsM5BjVOVJNK65ssnll6TUvs4ohvc="));
        profile.setProperties(properties);
        profile.complete();
        if(profile.isComplete()) {
            NPC.Builder builder = new NPC.Builder(profile)
                    .location(spawnLocation)
                    .lookAtPlayer(true)
                    .imitatePlayer(false);
            npc = builder.build(BedwarsPractice.instance.npcPool);
            for(Player player : Bukkit.getOnlinePlayers()) {
                player.sendMessage("Initialized NPC at location: " + npc.getLocation());
            }
            npc.getExcludedPlayers().forEach(player -> player.sendMessage("You can't see me in world: "+ npc.getLocation().getWorld().getName()));
            npc.getSeeingPlayers().forEach(player -> player.sendMessage("You can see me in world: "+ npc.getLocation().getWorld().getName()));
        }
        else
        {
            for(Player player : Bukkit.getOnlinePlayers())
                player.sendMessage("Shop NPC profile was not complete");
        }
        Bukkit.getPluginManager().registerEvents(this, BedwarsPractice.instance);
    }
    public void Endgame()
    {
        HandlerList.unregisterAll(this);
    }
    public void OpenShop(Player player)
    {
        CreateShopInventory(player);
    }
    public void CreateShopInventory(Player openingPlayer)
    {
        Shop shop = new Shop(type);
        switch (type) {
            case UPGRADES:
                shop.addItemToShop(0, new ShopItem(() -> "Sharpness", () -> "Adds Sharpness I to your sword", Material.IRON_SWORD, 1, Material.DIAMOND, () -> 4, player -> {
                    game.GetBedwarsPlayerFromPlayer(player).team.AddSharpness();
                    return true;
                }));
                shop.addItemToShop(1, new ShopItem(() -> "Protection " + IntegerToRomanNumeral(Integer.min(game.GetBedwarsPlayerFromPlayer(openingPlayer).team.protectionLevel+1,4)), () -> "Adds Protection to your armor", Material.IRON_CHESTPLATE, 1, Material.DIAMOND, () ->
                {
                    switch (game.GetBedwarsPlayerFromPlayer(openingPlayer).team.protectionLevel)
                    {
                        case 0:
                            return 2;
                        case 1:
                            return 4;
                        case 2:
                            return 8;
                        case 3:
                            return 16;
                        default:
                            return Integer.MAX_VALUE;
                    }
                }, player -> {
                    BedwarsTeam team = game.GetBedwarsPlayerFromPlayer(player).team;
                    if(team.protectionLevel == 4)
                        return false;
                    else
                        team.AddProtection();
                    return true;
                }));
                break;
            case ITEMS:
                shop.addItemToShop(0, new ShopItem(() -> "Wool x 16", () -> "Good for bridging and defending.", Material.WOOL, 16, Material.IRON_INGOT, () -> 4, (player -> {
                    player.getInventory().addItem(new ItemStack(Material.WOOL, 16));
                    return true;
                })));
                shop.addItemToShop(1, new ShopItem(() -> "Wood x 16", () -> "Can be used to bridge,\n but is mainly for defending", Material.WOOD, 16, Material.GOLD_INGOT, () -> 4, player -> {
                    player.getInventory().addItem(new ItemStack(Material.WOOD, 16));
                    return true;
                }));
                shop.addItemToShop(2, new ShopItem(() -> "End Stone x 16", () -> "If you bridge with this,\n you're wasting money.\n PLEASE DEFEND.", Material.ENDER_STONE, 12, Material.IRON_INGOT, () -> 24, player -> {
                    player.getInventory().addItem(new ItemStack(Material.ENDER_STONE, 12));
                    return true;
                }));
                shop.addItemToShop(3, new ShopItem(() -> "Obsidian x 4", () -> "Only good for defending,\n DO NOT BRIDGE", Material.OBSIDIAN, 4, Material.EMERALD, () -> 4, player -> {
                    player.getInventory().addItem(new ItemStack(Material.OBSIDIAN, 4));
                    return true;
                }));
                shop.addItemToShop(4, new ShopItem(() -> "Stone Sword", () -> "Slightly better then a wood sword", Material.STONE_SWORD, 1, Material.IRON_INGOT, () -> 10, player -> {
                    game.GetBedwarsPlayerFromPlayer(player).GiveSword(Material.STONE_SWORD);
                    return true;
                }));
                shop.addItemToShop(5, new ShopItem(() -> "Iron Sword", () -> "Much better then a wood sword", Material.IRON_SWORD, 1, Material.GOLD_INGOT, () -> 7, player -> {
                    game.GetBedwarsPlayerFromPlayer(player).GiveSword(Material.IRON_SWORD);
                    return true;
                }));
                shop.addItemToShop(6, new ShopItem(() -> "Diamond Sword", () -> "As far from a wood sword\n as you can get!", Material.DIAMOND_SWORD, 1, Material.EMERALD, () -> 4, player -> {
                    game.GetBedwarsPlayerFromPlayer(player).GiveSword(Material.DIAMOND_SWORD);
                    return true;
                }));
                shop.addItemToShop(7, new ShopItem(() -> "Chain-mail Armor", () -> "A little protection from attacks", Material.CHAINMAIL_BOOTS, 1, Material.IRON_INGOT, () -> 40, player -> {
                    game.GetBedwarsPlayerFromPlayer(player).armorLevel = "CHAIN";
                    game.GetBedwarsPlayerFromPlayer(player).GiveArmor();
                    return true;
                }));
                shop.addItemToShop(8, new ShopItem(() -> "Iron Armor", () -> "More protection from attacks", Material.IRON_BOOTS, 1, Material.GOLD_INGOT, () -> 12, player -> {
                    game.GetBedwarsPlayerFromPlayer(player).armorLevel = "IRON";
                    game.GetBedwarsPlayerFromPlayer(player).GiveArmor();
                    return true;
                }));
                shop.addItemToShop(9, new ShopItem(() -> "Diamond Armor", () -> "The most protection from attacks", Material.DIAMOND_BOOTS, 1, Material.EMERALD, () -> 6, player -> {
                    game.GetBedwarsPlayerFromPlayer(player).armorLevel = "DIAMOND";
                    game.GetBedwarsPlayerFromPlayer(player).GiveArmor();
                    return true;
                }));
                shop.addItemToShop(10, new ShopItem(() -> "Boom Boom", () -> "Very usefully for breaking through a base defense", Material.TNT, 1, Material.GOLD_INGOT, () -> 4, player -> {
                    game.GetBedwarsPlayerFromPlayer(player).player.getInventory().addItem(new ItemStack(Material.TNT));
                    return true;
                }));
                shop.addItemToShop(11, new ShopItem(() -> "Wooden Pickaxe", () -> "Can break through anything but obsidian", Material.WOOD_PICKAXE, 1, Material.IRON_INGOT, () -> 10, player -> {
                    ItemStack woodPickaxe = new ItemStack(Material.WOOD_PICKAXE);
                    ItemMeta woodPickaxeMeta = woodPickaxe.getItemMeta();
                    woodPickaxeMeta.spigot().setUnbreakable(true);
                    woodPickaxeMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
                    woodPickaxe.setItemMeta(woodPickaxeMeta);
                    woodPickaxe.addEnchantment(Enchantment.DIG_SPEED, 1);
                    game.GetBedwarsPlayerFromPlayer(player).player.getInventory().addItem(woodPickaxe);
                    return true;
                }));
                shop.addItemToShop(12, new ShopItem(() -> "Diamond Pickaxe", () -> "Can break through any base defense", Material.DIAMOND_PICKAXE, 1, Material.GOLD_INGOT, () -> 12, player -> {
                    ItemStack diamondPickaxe = new ItemStack(Material.DIAMOND_PICKAXE);
                    ItemMeta diamondPickaxeMeta = diamondPickaxe.getItemMeta();
                    diamondPickaxeMeta.spigot().setUnbreakable(true);
                    diamondPickaxeMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
                    diamondPickaxe.setItemMeta(diamondPickaxeMeta);
                    diamondPickaxe.addEnchantment(Enchantment.DIG_SPEED, 1);
                    player.getInventory().addItem(diamondPickaxe);
                    return true;
                }));
                shop.addItemToShop(13, new ShopItem(() -> "Shears", () -> "Don't worry about wool anymore!", Material.SHEARS, 1, Material.IRON_INGOT, () -> 20, player -> {
                    game.GetBedwarsPlayerFromPlayer(player).hasSheers = true;
                    game.GetBedwarsPlayerFromPlayer(player).GiveShears();
                    return true;
                }));
                shop.addItemToShop(14, new ShopItem(() -> "Fire Balls!", () -> "Can anything but endstone and obsidian.\nBOOM!", Material.FIREBALL, 1, Material.IRON_INGOT, () -> 40, player -> {
                    player.getInventory().addItem(new ItemStack(Material.FIREBALL));
                    return true;
                }));
                shop.addItemToShop(15, new ShopItem(() -> "Golden Apple", () -> "Regenerates health faster\nas well\n as giving you two extra hearts", Material.GOLDEN_APPLE, 1, Material.GOLD_INGOT, () -> 3, player -> {
                    player.getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE));
                    return true;
                }));
                break;
        }
        openingPlayer.openInventory(shop.getInventory());
    }
    public static String IntegerToRomanNumeral(int input) {
        if (input < 1 || input > 3999)
            return "Invalid Roman Number Value";
        String s = "";
        while (input >= 1000) {
            s += "M";
            input -= 1000;        }
        while (input >= 900) {
            s += "CM";
            input -= 900;
        }
        while (input >= 500) {
            s += "D";
            input -= 500;
        }
        while (input >= 400) {
            s += "CD";
            input -= 400;
        }
        while (input >= 100) {
            s += "C";
            input -= 100;
        }
        while (input >= 90) {
            s += "XC";
            input -= 90;
        }
        while (input >= 50) {
            s += "L";
            input -= 50;
        }
        while (input >= 40) {
            s += "XL";
            input -= 40;
        }
        while (input >= 10) {
            s += "X";
            input -= 10;
        }
        while (input >= 9) {
            s += "IX";
            input -= 9;
        }
        while (input >= 5) {
            s += "V";
            input -= 5;
        }
        while (input >= 4) {
            s += "IV";
            input -= 4;
        }
        while (input >= 1) {
            s += "I";
            input -= 1;
        }
        return s;
    }
    public enum ShopType
    {
        UPGRADES {
            public String toString()
            {
                return "Upgrade Shop";
            }
        },
        ITEMS {
            public String toString() {
                return "Item Shop";
            }
        }
    }
    @EventHandler
    public void clickedOnShop(PlayerNPCInteractEvent event)
    {
        if(event.getNPC() == npc)
        {
            Player player = event.getPlayer();
            OpenShop(player);
        }
    }
}

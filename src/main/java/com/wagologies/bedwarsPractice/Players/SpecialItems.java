package com.wagologies.bedwarsPractice.Players;

import com.wagologies.bedwarsPractice.BedwarsPractice;
import com.wagologies.bedwarsPractice.Commands.Bedwars;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class SpecialItems implements Listener {
    public static SpecialItems instance;
    public SpecialItems()
    {
        Bukkit.getPluginManager().registerEvents(this, BedwarsPractice.instance);
        instance = this;
    }
    public static void Stop()
    {
        HandlerList.unregisterAll(instance);
    }
    @EventHandler
    public void FireBallLaunch(PlayerInteractEvent event)
    {
        if(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)
        {
            if(event.getItem() != null && event.getItem().getType() == Material.FIREBALL)
            {
                if(!event.isCancelled())
                    event.setCancelled(true);
                ItemStack fireBall = event.getPlayer().getItemInHand();
                int consume = 1;
                if(event.getPlayer().getGameMode() == GameMode.CREATIVE)
                    consume = 0;
                if (fireBall.getAmount() == consume) {
                    event.getPlayer().getInventory().removeItem(new ItemStack(fireBall));
                } else {
                    fireBall.setAmount(fireBall.getAmount() - consume);
                }
                Fireball fireball = event.getPlayer().launchProjectile(Fireball.class);
                fireball.setIsIncendiary(true);
                fireball.setMetadata("BoomBall", new FixedMetadataValue(BedwarsPractice.instance, "fireball"));
            }
        }
    }
    @EventHandler
    public void FireBallImpact(ProjectileHitEvent event)
    {
        Entity entity = event.getEntity();

        if (entity instanceof Fireball) {
            Fireball fireball = (Fireball) entity;
            if (!fireball.hasMetadata("BoomBall")) {
                return;
            }
            setKnockback(fireball,2*1.4);
        }
    }
    public boolean setKnockback(Entity center, double radius) {

        Location target = center.getLocation();

        int maxHeight = 8;

        List<Entity> nearbyEntities = center.getNearbyEntities(radius, radius, radius);

        if ((nearbyEntities == null) || nearbyEntities.isEmpty()) {
            return false;
        }

        List<LivingEntity> validEntities = new ArrayList<>();

        for (Entity entity : nearbyEntities) {
            if (entity.isValid() && (entity instanceof LivingEntity)) {
                validEntities.add((LivingEntity) entity);
            }
        }

        for (LivingEntity entity : validEntities) {

            if ((entity instanceof Player)) {
                Player player = (Player) entity;
                if (player.isFlying()||player.isConversing()) {
                    continue;
                }

            }

            double distance = (maxHeight - entity.getLocation().distance(target));

            double TWO_PI = 1.76 * Math.PI;

            Vector variantVel = entity.getLocation().getDirection().multiply(-1);
            variantVel = variantVel.setY(distance / TWO_PI);

            entity.setVelocity(variantVel);

        }

        return true;
    }
    @EventHandler
    public void AutoTNT(BlockPlaceEvent event)
    {
        if(event.getBlockPlaced().getType() == Material.TNT)
        {
            if(!event.isCancelled())
                event.setCancelled(true);
            ItemStack tntItem = event.getPlayer().getItemInHand();
            int consume = 1;
            if(event.getPlayer().getGameMode() == GameMode.CREATIVE)
                consume = 0;
            if (tntItem.getAmount() == consume) {
                event.getPlayer().getInventory().removeItem(new ItemStack(tntItem));
            } else {
                tntItem.setAmount(tntItem.getAmount() - consume);
            }
            Location l = new Location(event.getBlockPlaced().getWorld(), event.getBlockPlaced().getLocation().getBlockX()+0.5, event.getBlockPlaced().getLocation().getBlockY(), event.getBlockPlaced().getLocation().getBlockZ()+0.5);
            TNTPrimed tnt = event.getBlockPlaced().getLocation().getWorld().spawn(l, TNTPrimed.class);
            tnt.setYield(4);
        }
    }
    @EventHandler
    public void blockBoom(EntityExplodeEvent event) {

        List<Material> fireBallExploadableBlocks = new ArrayList<>();
        fireBallExploadableBlocks.add(Material.WOOL);
        fireBallExploadableBlocks.add(Material.WOOD);
        fireBallExploadableBlocks.add(Material.HARD_CLAY);
        if(event.getEntityType() == EntityType.PRIMED_TNT)
            fireBallExploadableBlocks.add(Material.ENDER_STONE);
        BedwarsPractice.instance.getLogger().info(String.valueOf(event.getEntityType()));
        for (Block block : event.blockList().toArray(new Block[] {})) {
            Material type = block.getType();

            if (!fireBallExploadableBlocks.contains(type)) {
                event.blockList().remove(block);
            }

        }

    }
}

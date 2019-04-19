package com.snowgears.grapplinghook;

import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.util.Vector;

import com.snowgears.grapplinghook.api.HookAPI;


public class GrapplingListener implements Listener{

	public GrapplingHook plugin;

	public HashMap<Integer, Integer> noFallEntities = new HashMap<Integer, Integer>(); //entity id, delayed task id
	public HashMap<String, Integer> noGrapplePlayers = new HashMap<String, Integer>(); //name, delayed task id
	
	public GrapplingListener(GrapplingHook instance) {
		plugin = instance;
	}
	
	@EventHandler
	public void onPreCraft(CraftItemEvent event){
		plugin.logMessage(Level.INFO, "Crafting item");
		if(plugin.usePerms) {
			plugin.logMessage(Level.INFO, "with permissions option.");
			if(event.getView().getPlayer() instanceof Player) {
				plugin.logMessage(Level.INFO, "Player is crafting.");
				Player player = (Player)event.getView().getPlayer();
				if(HookAPI.isGrapplingHook(event.getInventory().getResult())) {
					if(event.getInventory().contains(Material.ACACIA_PLANKS)
							|| event.getInventory().contains(Material.BIRCH_PLANKS)
							|| event.getInventory().contains(Material.DARK_OAK_PLANKS)
							|| event.getInventory().contains(Material.JUNGLE_PLANKS)
							|| event.getInventory().contains(Material.OAK_PLANKS)
							|| event.getInventory().contains(Material.SPRUCE_PLANKS)){
						plugin.logMessage(Level.INFO, "Wooden.");
						if(!player.hasPermission("grapplinghook.craft.wood"))
							event.setCancelled(true);
					}
					else if(event.getInventory().contains(Material.COBBLESTONE)) {
						plugin.logMessage(Level.INFO, "Stone.");
						if(!player.hasPermission("grapplinghook.craft.stone"))
							event.setCancelled(true);
					}
					else if(event.getInventory().contains(Material.IRON_INGOT)) {
						plugin.logMessage(Level.INFO, "Iron.");
						if(!player.hasPermission("grapplinghook.craft.iron"))
							event.setCancelled(true);
					}
					else if(event.getInventory().contains(Material.GOLD_INGOT)) {
						plugin.logMessage(Level.INFO, "Golden.");
						if(!player.hasPermission("grapplinghook.craft.gold"))
							event.setCancelled(true);
					}
					else if(event.getInventory().contains(Material.DIAMOND)) {
						plugin.logMessage(Level.INFO, "Diamond.");
						if(!player.hasPermission("grapplinghook.craft.diamond"))
							event.setCancelled(true);
					}
				}
			}
		}
	}

    @EventHandler
    public void onAttack(EntityDamageByEntityEvent event) {
		plugin.logMessage(Level.INFO, "Damage event");
		if (event.getDamager() instanceof FishHook) {
			plugin.logMessage(Level.INFO, "with hook");
    		FishHook hook = (FishHook) event.getDamager();
    		if (hook.getShooter() instanceof Player) {
				plugin.logMessage(Level.INFO, "by player.");
				Player player = (Player)hook.getShooter();

				if (HookAPI.isGrapplingHook(player.getInventory().getItemInMainHand())) {
					plugin.logMessage(Level.INFO, "Used a grappling hook");

					if (event.getEntity() instanceof Player && (!plugin.usePerms || player.hasPermission("grapplinghook.pull.players"))) {
						plugin.logMessage(Level.INFO, "to catch a player");
						Player hooked = (Player)event.getEntity();
						if (plugin.usePerms && hooked.hasPermission("grapplinghook.player.nopull")) {
							plugin.logMessage(Level.INFO, "but he refused.");
							event.setCancelled(true);
						}
						else {
							plugin.logMessage(Level.INFO, "successfully");
							hooked.sendMessage(ChatColor.YELLOW + "You have been hooked by " + ChatColor.RESET + player.getName() + ChatColor.YELLOW + "!");
							player.sendMessage(ChatColor.GOLD + "You have hooked " + ChatColor.RESET + hooked.getName() + ChatColor.YELLOW + "!");
						}
					}
					else {
						plugin.logMessage(Level.INFO, "to catch an entity.");
						String entityName = event.getEntityType().toString().replace("_", " ").toLowerCase();
						player.sendMessage(ChatColor.GOLD + "You have hooked a " + entityName + "!");
					}
				}
			}
		}
	}
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageEvent(EntityDamageEvent event) {
        if (event.getCause() == DamageCause.FALL) {
        	if (!plugin.fallDamage){
                if (noFallEntities.containsKey(event.getEntity().getEntityId()))
                    event.setCancelled(true);
        	}
        }
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onGrapple(PlayerGrappleEvent event){
		plugin.logMessage(Level.INFO, "Player grapple event");
    	if(!event.isCancelled()) {
			plugin.logMessage(Level.INFO, "The event is not cancelled");
            final Player player = event.getPlayer();

            if (event.getHookItem().getItemMeta() instanceof Damageable) {
                Damageable im = (Damageable) event.getHookItem().getItemMeta();
				plugin.logMessage(Level.INFO, "Resetting damage");
                im.setDamage(0);
                event.getHookItem().setItemMeta((ItemMeta) im);
            }

			if (plugin.usePerms && !player.hasPermission("grapplinghook.player.nocooldown") && noGrapplePlayers.containsKey(player.getName())){
				plugin.logMessage(Level.INFO, "The player is on cooldown.");
				player.sendMessage(ChatColor.GRAY + "You cannot do that yet.");
			}
			else {
				plugin.logMessage(Level.INFO, "Player not in cooldown.");
				Entity e = event.getPulledEntity();
				Location loc = event.getPullLocation();

				if (player.equals(e)){ //the player is pulling itself to a location
					plugin.logMessage(Level.INFO, "Pulling itself");
					if (plugin.teleportHooked) {
						plugin.logMessage(Level.INFO, "with teleport option.");
						loc.setPitch(player.getLocation().getPitch());
						loc.setYaw(player.getLocation().getYaw());
						player.teleport(loc);
					}
					else {
						plugin.logMessage(Level.INFO, "without teleport option");
						if (player.getLocation().distance(loc) < 6) { //hook is too close to player
							pullPlayerSlightly(player, loc);
							plugin.logMessage(Level.INFO, "slightly.");
						}
						else {
							pullEntityToLocation(player, loc);
							plugin.logMessage(Level.INFO, "at normal velocity.");
						}
					}
				}
				else { //the player is pulling an entity to them
					plugin.logMessage(Level.INFO, "Pulling an entity");
					if (plugin.teleportHooked) {
						plugin.logMessage(Level.INFO, "with teleport option.");
						e.teleport(loc);
					}
					else {
						plugin.logMessage(Level.INFO, "without teleport option.");
						pullEntityToLocation (e, loc);
						if (e instanceof Item){
							plugin.logMessage(Level.INFO, "The entity was an item");
							ItemStack is = ((Item)e).getItemStack();
							String itemName = is.getType().toString().replace("_", " ").toLowerCase();
							player.sendMessage(ChatColor.GOLD + "You have hooked a stack of " + is.getAmount() + " " + itemName + "!");
						}
					}
				}

				if(HookAPI.addUse(player, event.getHookItem())) {
					plugin.logMessage(Level.INFO, "Damaged grapple.");
					HookAPI.playGrappleSound(player.getLocation());
				}

				if(plugin.timeBetweenUses != 0) {
					plugin.logMessage(Level.INFO, "Adding " + plugin.timeBetweenUses + " seconds cooldown!");
					HookAPI.addPlayerCooldown(player, plugin.timeBetweenUses);
				}
			}
        }
    }

    private Entity getFirstItem(List<Entity> entityList) {
	    Entity res = null;

        for(Entity ent : entityList) {
            if(ent instanceof Item){
                res = ent;
            }
        }

        return res;
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void fishEvent(PlayerFishEvent event) //called before projectileLaunchEvent
    {
        Player player = event.getPlayer();
		plugin.logMessage(Level.INFO, "Fishing event");

        if(HookAPI.isGrapplingHook(player.getInventory().getItemInMainHand())) {
			plugin.logMessage(Level.INFO, "Grappling Hook in hand");
            switch (event.getState()) {
                case IN_GROUND:
					plugin.logMessage(Level.INFO, "Hook in ground");
                    Location loc = event.getHook().getLocation();
                    Entity grappledItem = getFirstItem(event.getHook().getNearbyEntities(1.5, 1, 1.5));
                    if (grappledItem != null && (!plugin.usePerms || player.hasPermission("grapplinghook.pull.items"))) {
                        PlayerGrappleEvent e = new PlayerGrappleEvent(player, grappledItem, player.getLocation());
                        plugin.getServer().getPluginManager().callEvent(e);
                    }
                    else if(!plugin.usePerms || player.hasPermission("grapplinghook.pull.self")) {
                        PlayerGrappleEvent e = new PlayerGrappleEvent(player, player, loc);
                        plugin.getServer().getPluginManager().callEvent(e);
                    }
                    break;
                case CAUGHT_ENTITY:
					plugin.logMessage(Level.INFO, "Caught entity");
                    if(event.getCaught() instanceof Player){
                        Player hooked = (Player) event.getCaught();
                        if(plugin.usePerms && hooked.hasPermission("grapplinghook.player.nopull")){
                            player.sendMessage(ChatColor.GRAY + hooked.getName()+" can not be pulled with grappling hooks.");
                        }
                        else if(!plugin.usePerms || player.hasPermission("grapplinghook.pull.players")){
                            PlayerGrappleEvent e = new PlayerGrappleEvent(player, hooked, player.getLocation());
                            plugin.getServer().getPluginManager().callEvent(e);
                        }
                    }
                    else if(!plugin.usePerms || player.hasPermission("grapplinghook.pull.mobs")){
                        PlayerGrappleEvent e = new PlayerGrappleEvent(player, event.getCaught(), player.getLocation());
                        plugin.getServer().getPluginManager().callEvent(e);
                    }
                    else {
                        player.sendMessage(ChatColor.GRAY + "You can not pull entities!");
                    }
                    break;
				case CAUGHT_FISH:
					plugin.logMessage(Level.INFO, "Trying to fish with a hook. Cancelling.");
					event.setCancelled(true);
				default:
					plugin.logMessage(Level.INFO, "Not in ground or entity. " + event.getState().toString());
            }
		}
    }

//	//FOR HOOKING AN ITEM AND PULLING TOWARD YOU
//	public void pullItemToLocation(Item i, Location loc){
//		ItemStack is = i.getItemStack();
//		i.getWorld().dropItemNaturally(loc, is);
//		i.remove();
//	}
//	
//	//FOR HOOKING AN ITEM AND PULLING TOWARD YOU
//	public void pullItemToLocation(Entity e, Location loc){
//		Location oLoc = e.getLocation().add(0, 1, 0);
//		Location pLoc = loc;
//	
//		// Velocity from Minecraft Source. 
//		double d1 = pLoc.getX() - oLoc.getX();
//		double d3 = pLoc.getY() - oLoc.getY();
//		double d5 = pLoc.getZ() - oLoc.getZ();
//		double d7 = ((float) Math
//				.sqrt((d1 * d1 + d3 * d3 + d5 * d5)));
//		double d9 = 0.10000000000000001D;
//		double motionX = d1 * d9;
//		double motionY = d3 * d9 + (double) ((float) Math.sqrt(d7))
//				* 0.080000000000000002D;
//		double motionZ = d5 * d9;
//		e.setVelocity(new Vector(motionX, motionY, motionZ));
//	}
	
//	//FOR HOOKING AN ENTITY AND PULLING TOWARD YOU
//	private void pullEntityToLocation(Entity e, Location loc){
//		Location entityLoc = e.getLocation();
//		
//		double dX = entityLoc.getX() - loc.getX();
//		double dY = entityLoc.getY() - loc.getY();
//		double dZ = entityLoc.getZ() - loc.getZ();
//		
//		double yaw = Math.atan2(dZ, dX);
//		double pitch = Math.atan2(Math.sqrt(dZ * dZ + dX * dX), dY) + Math.PI;
//		
//		double X = Math.sin(pitch) * Math.cos(yaw);
//		double Y = Math.sin(pitch) * Math.sin(yaw);
//		double Z = Math.cos(pitch);
//		 
//		Vector vector = new Vector(X, Z, Y);
//		e.setVelocity(vector.multiply(8));
//	}
	
	//For pulling a player slightly
	private void pullPlayerSlightly(Player p, Location loc){
		if(loc.getY() > p.getLocation().getY()){
			p.setVelocity(new Vector(0,0.25,0));
			return;
		}
		
		Location playerLoc = p.getLocation();
		
		Vector vector = loc.toVector().subtract(playerLoc.toVector());
		p.setVelocity(vector);
	}
	
//	//Code from r306 roll the dice
//	private void pullEntityToLocation(final Entity e, Location loc){
//		Location entityLoc = e.getLocation();
//			
//		final Vector velocity = loc.toVector().subtract(entityLoc.subtract(0, 1, 0).toVector()).normalize().multiply(new Vector(2, 2, 2));
//		
//		if (Math.abs(loc.getBlockY() - entityLoc.getBlockY()) < 2 && loc.distance(entityLoc) > 4)
//		{
//
//			e.setVelocity(velocity.multiply(new Vector(1, 1, 1)));
//
//			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
//			{
//				@Override
//				public void run() 
//				{
//					e.setVelocity(velocity.multiply(new Vector(1, 1, 1)));
//					//player.setVelocity(location.toVector().subtract(player.getLocation().subtract(0, 1, 0).toVector().normalize().multiply(2)));
//
//				}
//
//			}, 1L);
//		}
//		else
//		{
//			e.setVelocity(velocity);
//
//			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
//			{
//				@Override
//				public void run() 
//				{
//					e.setVelocity(velocity.multiply(new Vector(1, 1, 1)));
//					//player.setVelocity(location.toVector().subtract(player.getLocation().subtract(0, 1, 0).toVector().normalize().multiply(0.5)));
//				}
//			}, 0L);
//		}
//		addNoFall(e, 100);
//	}
	
	//better method for pulling
	private void pullEntityToLocation(final Entity e, Location loc){
		Location entityLoc = e.getLocation();

		entityLoc.setY(entityLoc.getY()+0.5);
		e.teleport(entityLoc);
		
		double g = -0.08;
		double t = loc.distance(entityLoc);
		double v_x = (1.0+0.07*t) * (loc.getX()-entityLoc.getX())/t;
		double v_y = (1.0+0.03*t) * (loc.getY()-entityLoc.getY())/t -0.5*g*t;
		double v_z = (1.0+0.07*t) * (loc.getZ()-entityLoc.getZ())/t;
		
		Vector v = e.getVelocity();
		v.setX(v_x);
		v.setY(v_y);
		v.setZ(v_z);
		e.setVelocity(v);
		
		addNoFall(e, 100);
	}
	
	public void addNoFall(final Entity e, int ticks) {
		if(noFallEntities.containsKey(e.getEntityId()))
			Bukkit.getServer().getScheduler().cancelTask(noFallEntities.get(e.getEntityId()));
		
		int taskId = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			 @Override
			public void run(){
				  if(noFallEntities.containsKey(e.getEntityId()))
					 noFallEntities.remove(e.getEntityId());
			  }
	  	}, ticks);
		
		noFallEntities.put(e.getEntityId(), taskId);
	}
}
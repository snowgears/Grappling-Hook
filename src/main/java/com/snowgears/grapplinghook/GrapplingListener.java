package com.snowgears.grapplinghook;

import com.snowgears.grapplinghook.api.HookAPI;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;


public class GrapplingListener implements Listener{

	public GrapplingHook plugin;

	public HashMap<Integer, Integer> noFallEntities = new HashMap<Integer, Integer>(); //entity id, delayed task id
	public HashMap<UUID, Integer> noGrapplePlayers = new HashMap<>(); //uuid, delayed task id
	private HashMap<UUID, FishHook> activeHookEntities = new HashMap<>(); //player uuid, ref to hook entity
	private HashMap<UUID, Location> hookLastLocation = new HashMap<>(); //player uuid, location of hook entity
	private HashSet<UUID> playersConsumedSlowfall = new HashSet<>();

	private int HOOK_BREAK_DISTANCE_SQUARED = 1156; //34 is hook break distance

	public GrapplingListener(GrapplingHook instance) {
		plugin = instance;
	}
	
	@EventHandler
	public void onPreCraft(CraftItemEvent event){
		if(plugin.usePerms() == false)
			return;
		if(event.getView().getPlayer() instanceof Player){
			Player player = (Player)event.getView().getPlayer();
			if(player.isOp())
				return;
			if(HookAPI.isGrapplingHook(event.getInventory().getResult())){
				ItemStack resultHook = event.getInventory().getResult();
				int recipeNum = HookAPI.getHookRecipeNumber(resultHook);
				if(!player.hasPermission("grapplinghook.craft."+recipeNum)) {
					event.setCancelled(true);
					return;
				}
			}
		}
	}

    @EventHandler
    public void onHookHitEntity(ProjectileHitEvent event){
    	
		if(event.getEntity() instanceof FishHook){
    		FishHook hook = (FishHook)event.getEntity();
    		if( ! (hook.getShooter() instanceof Player))
    			return;
    		Player player = (Player)hook.getShooter();

    		if(HookAPI.isGrapplingHook(player.getInventory().getItemInMainHand()) == false)
    			return;

			if(event.getHitEntity() == null)
				return;

			if(plugin.getRecipeLoader().isEntityOnHookBlacklist(player, event.getHitEntity())){

				final ItemStack curItemInHand = player.getInventory().getItemInMainHand().clone();
				player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
				//run task 2 ticks later
				Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
					player.getInventory().setItemInMainHand(curItemInHand);
				}, 2);

				return;
			}
    		
    		if(plugin.usePerms() == false || player.hasPermission("grapplinghook.pull.players")){
	    		
	    		if(event.getHitEntity() instanceof Player){
		    		Player hooked = (Player)event.getHitEntity();
		    		if(hooked.hasPermission("grapplinghook.player.nopull")){
		    			//event.setCancelled(true);
						final ItemStack curItemInHand = player.getInventory().getItemInMainHand().clone();
						player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
						//run task 2 ticks later
						Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
							player.getInventory().setItemInMainHand(curItemInHand);
						}, 2);
		    		}
		    		else{
		    			//hooked.sendMessage(ChatColor.YELLOW+"You have been hooked by "+ ChatColor.RESET+player.getName()+ChatColor.YELLOW+"!");
		    			//player.sendMessage(ChatColor.GOLD+"You have hooked "+ChatColor.RESET+hooked.getName()+ChatColor.YELLOW+"!");
		    		}
	    		}
	    		else{
	    			//String entityName = event.getHitEntity().getType().toString().replace("_", " ").toLowerCase();
	    			//player.sendMessage(ChatColor.GOLD+"You have hooked a "+entityName+"!");
	    		}
	    	}
		}
	}
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageEvent(EntityDamageEvent event) {
        if (event.getCause() == DamageCause.FALL) {
        	if(plugin.getTeleportHooked()){
        		return;
        	}
        	if(noFallEntities.containsKey(event.getEntity().getEntityId()))
        		event.setCancelled(true);
        }
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onGrapple(PlayerGrappleEvent event){
    	if(event.isCancelled())
    		return;
    	final Player player = event.getPlayer();
    	
    	event.getHookItem().setDurability((short)-10);

		if(activeHookEntities.containsKey(player.getUniqueId())){
			activeHookEntities.remove(player.getUniqueId());
		}

    	if(noGrapplePlayers.containsKey(player.getUniqueId())){
    		if((plugin.usePerms() && !player.hasPermission("grapplinghook.player.nocooldown")) || (!plugin.usePerms() && !player.isOp())){
    			player.sendMessage(ChatColor.GRAY+"You cannot do that yet.");
    			return;
    		}
    	}
    	
    	Entity e = event.getPulledEntity();
    	Location loc = event.getPullLocation();
    	
    	if(player.equals(e)){ //the player is pulling themself to a location
	    	if(plugin.getTeleportHooked()){
	    		loc.setPitch(player.getLocation().getPitch());
	    		loc.setYaw(player.getLocation().getYaw());
	        	player.teleport(loc);
	    	}
	    	else{
	    		if(player.getLocation().distance(loc) < 6) //hook is too close to player
	    			pullPlayerSlightly(player, loc);
	    		else
					pullEntityToLocation(player, loc, HookAPI.getHookInHandVelocityPull(player));
	    	}
    	}
    	else{ //the player is pulling an entity to them
    		if(plugin.getTeleportHooked()) {
				e.teleport(loc);
			}
	    	else{
				pullEntityToLocation(e, loc,  HookAPI.getHookInHandVelocityPull(player));

//	    		if(e instanceof Item){
//	    			ItemStack is = ((Item)e).getItemStack();
//	    			String itemName = is.getType().toString().replace("_", " ").toLowerCase();
//	    			player.sendMessage(ChatColor.GOLD+"You have hooked a stack of "+is.getAmount()+" "+itemName+"!");
//	    		}
	    	}
    	}

    	if(HookAPI.addUse(player, event.getHookItem())) {
			HookAPI.playGrappleSound(player.getLocation());
			ItemStack curItemInHand = event.getHookItem().clone();

			int timeBetweenGrapples = HookAPI.getHookInHandTimeBetweenGrapples(player);
			if(timeBetweenGrapples > 0){
				HookAPI.addPlayerCooldown(player, timeBetweenGrapples);
			}

			player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));

			//run task 2 ticks later
			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
				player.getInventory().setItemInMainHand(curItemInHand);
			}, 2);
		}
    }

    @EventHandler
	public void onLineBreak(FishingLineBreakEvent event){
		//event.getPlayer().sendMessage("FishingLineBreakEvent called.");
		Player player = event.getPlayer();
		player.getWorld().playSound(player.getLocation(), Sound.BLOCK_TRIPWIRE_DETACH, 1f, 1f);
		//stop falling when line snaps
		if(event.getHookLocation().getY() > player.getLocation().getY()){
			player.setVelocity(player.getVelocity().setY(0));
		}
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event){
		if(activeHookEntities.containsKey(event.getPlayer().getUniqueId())){
			FishHook hook = activeHookEntities.get(event.getPlayer().getUniqueId());
			if(hook != null && !hook.isDead()){
				Block belowHook = hook.getLocation().getBlock().getRelative(BlockFace.DOWN);
				if(belowHook.isPassable())
					return;
			}
			else
				return;

			if(playersConsumedSlowfall.contains(event.getPlayer().getUniqueId())){
				Block belowPlayer = event.getPlayer().getLocation().getBlock().getRelative(BlockFace.DOWN);
				if(!belowPlayer.isPassable()){
					if(HookAPI.isGrapplingHook(event.getPlayer().getInventory().getItemInMainHand())) {
						HookAPI.addUse(event.getPlayer(), event.getPlayer().getInventory().getItemInMainHand());
						playersConsumedSlowfall.remove(event.getPlayer().getUniqueId());
					}
				}
			}

			Location hookLoc = hookLastLocation.get(event.getPlayer().getUniqueId());
			if(hookLoc != null && hookLoc.getY() > event.getPlayer().getLocation().getY()) {
				if(HookAPI.isGrapplingHook(event.getPlayer().getInventory().getItemInMainHand())) {
					if(HookAPI.getHookInHandHasSlowFall(event.getPlayer())) {
						event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 3, 1));
						if(plugin.isConsumeUseOnSlowfall()) {
							Block belowPlayer = event.getPlayer().getLocation().getBlock().getRelative(BlockFace.DOWN);
							if(belowPlayer.isPassable()) {
								playersConsumedSlowfall.add(event.getPlayer().getUniqueId());
							}
						}
					}
				}
			}
		}
	}

	@EventHandler
	public void hookStuck(ProjectileHitEvent event) {
		if (event.getEntity() instanceof FishHook && event.getEntity().getShooter() instanceof Player) {
			if(activeHookEntities.containsKey(((Player) event.getEntity().getShooter()).getUniqueId())) {
				FishHook fishHook = (FishHook) event.getEntity();
				Player player = (Player) fishHook.getShooter();

				if(!HookAPI.getHookInHandHasStickyHook(player))
					return;

				if (event.getHitBlock() != null && !event.getHitBlock().getLocation().getBlock().isEmpty()) {
					Location hitblock = event.getHitBlock().getLocation().add(0.5, 0, 0.5);
					ArmorStand armorStand = player.getWorld().spawn(hitblock, ArmorStand.class);
					armorStand.addPassenger(fishHook);
					armorStand.setGravity(false);
					armorStand.setVisible(false);
					armorStand.setSmall(true);
					armorStand.setArms(false);
					armorStand.setMarker(true);
					armorStand.setBasePlate(false);
					fishHook.setGravity(false);
					fishHook.setBounce(true);
					fishHook.setMetadata("stuckBlock", new FixedMetadataValue(plugin, ""));
				}
			}
		}
	}
    
    @EventHandler (priority = EventPriority.HIGHEST)
    public void fishEvent(PlayerFishEvent event) //called before projectileLaunchEvent
    {
    	//System.out.println(event.getState().name());
        Player player = event.getPlayer();

        if(HookAPI.isGrapplingHook(player.getInventory().getItemInMainHand()) == false)
        	return;

		if(event.getState() == org.bukkit.event.player.PlayerFishEvent.State.IN_GROUND  || event.getState() == org.bukkit.event.player.PlayerFishEvent.State.FAILED_ATTEMPT || event.getHook().hasMetadata("stuckBlock")) {

			Location loc = event.getHook().getLocation();

			if(event.getHook().hasMetadata("stuckBlock")) {
				event.getHook().removeMetadata("stuckBlock", plugin);
				event.getHook().getVehicle().remove();
			}

			if(plugin.usePerms() == false || player.hasPermission("grapplinghook.pull.items")){
	        	for(Entity ent : event.getHook().getNearbyEntities(1.5, 1, 1.5)){
	        		if(ent instanceof Item){
	        			PlayerGrappleEvent e = new PlayerGrappleEvent(player, ent, player.getLocation());
	                	plugin.getServer().getPluginManager().callEvent(e);
	        			return;
	        		}
	        	}
			}
        	
			if(plugin.usePerms() == false || player.hasPermission("grapplinghook.pull.self")){
				PlayerGrappleEvent e = new PlayerGrappleEvent(player, player, loc);
				plugin.getServer().getPluginManager().callEvent(e);
			}
        }
        else if(event.getState() == org.bukkit.event.player.PlayerFishEvent.State.CAUGHT_ENTITY){
        	event.setCancelled(true);
        	if(event.getCaught() instanceof Player){
        		Player hooked = (Player)event.getCaught();
        		if(hooked.hasPermission("grapplinghook.player.nopull")){
	    			event.setCancelled(true);
	    			//player.sendMessage(ChatColor.GRAY+hooked.getName()+" can not be pulled with grappling hooks.");
	    		}
        		else if(plugin.usePerms() == false || player.hasPermission("grapplinghook.pull.players")){
            		PlayerGrappleEvent e = new PlayerGrappleEvent(player, hooked, player.getLocation());
                	plugin.getServer().getPluginManager().callEvent(e);
    			}
        	}
        	else if(plugin.usePerms() == false || player.hasPermission("grapplinghook.pull.mobs")){
        		PlayerGrappleEvent e = new PlayerGrappleEvent(player, event.getCaught(), player.getLocation());
            	plugin.getServer().getPluginManager().callEvent(e);
			}
        }
        else if(event.getState() == org.bukkit.event.player.PlayerFishEvent.State.CAUGHT_FISH){
        	event.setCancelled(true);
        }
        //casting the fishing line out
        else if(event.getState() == PlayerFishEvent.State.FISHING){
			activeHookEntities.put(player.getUniqueId(), event.getHook());

			event.getHook().setVelocity(event.getHook().getVelocity().multiply(HookAPI.getHookInHandVelocityThrow(player)));

			BukkitRunnable task = new BukkitRunnable() {
				@Override
				public void run() {
					if(activeHookEntities.containsKey(player.getUniqueId())) {
						FishHook hook = activeHookEntities.get(player.getUniqueId());
						if (hook == null || hook.isDead()) {

							Location hookLocation = hookLastLocation.get(player.getUniqueId());

							if(HookAPI.isGrapplingHook(player.getInventory().getItemInMainHand())) {
								boolean lineBreak = HookAPI.getHookInHandHasLineBreak(player);
								if(lineBreak) {
									FishingLineBreakEvent e = new FishingLineBreakEvent(player, hookLocation);
									Bukkit.getServer().getPluginManager().callEvent(e);
								}
							}
							activeHookEntities.remove(player.getUniqueId());
							this.cancel();
						}
						else{
							hookLastLocation.put(player.getUniqueId(), hook.getLocation());
						}
					}

				}
			};
			task.runTaskTimer(plugin, 1, 1);
		}
		else if(event.getState() == PlayerFishEvent.State.REEL_IN){
			if(HookAPI.getHookInHandHasAirHook(player)){
				if(plugin.usePerms() == false || player.hasPermission("grapplinghook.pull.self")){
					PlayerGrappleEvent e = new PlayerGrappleEvent(player, player, event.getHook().getLocation());
					plugin.getServer().getPluginManager().callEvent(e);
				}
			}
			else{
				if(activeHookEntities.containsKey(player.getUniqueId())) {
					activeHookEntities.remove(player.getUniqueId());
				}
				if(hookLastLocation.containsKey(player.getUniqueId())) {
					hookLastLocation.remove(player.getUniqueId());
				}
			}
		}
        else{
			if(activeHookEntities.containsKey(player.getUniqueId())) {
				activeHookEntities.remove(player.getUniqueId());
			}
			if(hookLastLocation.containsKey(player.getUniqueId())) {
				hookLastLocation.remove(player.getUniqueId());
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
	
	//better method for pulling
	private void pullEntityToLocation(final Entity e, Location loc, double multiply){
		Location entityLoc = e.getLocation();

		entityLoc.setY(entityLoc.getY()+0.5);
		e.teleport(entityLoc);
		
		double g = -0.08;
		double d = loc.distance(entityLoc);
		double t = d;
		double v_x = (1.0+0.07*t) * (loc.getX()-entityLoc.getX())/t;
		double v_y = (1.0+0.03*t) * (loc.getY()-entityLoc.getY())/t -0.5*g*t;
		double v_z = (1.0+0.07*t) * (loc.getZ()-entityLoc.getZ())/t;
		
		Vector v = e.getVelocity();
		v.setX(v_x);
		v.setY(v_y);
		v.setZ(v_z);
		v.multiply(multiply);
		e.setVelocity(v);

		if(e instanceof Player){
			Player player = (Player)e;
			if(HookAPI.isGrapplingHook(player.getInventory().getItemInMainHand())){
				boolean fallDamage = HookAPI.getHookInHandHasFallDamage(player);
				if(!fallDamage){
					addNoFall(player, 100);
				}
			}
		}
		else {
			addNoFall(e, 100);
		}
	}
	
	public void addNoFall(final Entity e, int ticks) {
		if(noFallEntities.containsKey(e.getEntityId()))
			Bukkit.getServer().getScheduler().cancelTask(noFallEntities.get(e.getEntityId()));
		
		int taskId = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin,new Runnable() {
			 @Override
			public void run(){
				  if(noFallEntities.containsKey(e.getEntityId()))
					 noFallEntities.remove(e.getEntityId());
			  }
	  	}, ticks);
		
		noFallEntities.put(e.getEntityId(), taskId);
	}
}